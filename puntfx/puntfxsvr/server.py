from flask import Flask
from flask_sockets import Sockets
from threading import Lock
import threading
import json
import gevent
import gevent.lock
import gevent.queue
import six.moves

app = Flask(__name__)
sockets = Sockets(app)

games = {"sample.json" : 2,
         "lambda.json" : 3,
         "sierpinski-triangle" : 3}
selected_game = "sample.json"
max_players = games[selected_game]

with open(selected_game) as data_file:
    map_data = json.load(data_file)
    # normalize the rivers
    for i in six.moves.range(len(map_data["rivers"])):
        if (map_data["rivers"][i]["source"] > map_data["rivers"][i]["target"]):
            temp = map_data["rivers"][i]["source"]
            map_data["rivers"][i]["source"] = map_data["rivers"][i]["target"]
            map_data["rivers"][i]["target"] = temp

class CountDownLatch(object):
    def __init__(self, count=1):
        self.count = count
        self.lock = threading.Condition()
        #self.lock = gevent.lock.BoundedSemaphore(1)

    def count_down(self):
        self.lock.acquire()
        self.count -= 1
        if self.count <= 0:
            self.lock.notifyAll()
        self.lock.release()

    def await(self):
        self.lock.acquire()
        while self.count > 0:
            self.lock.wait()
        self.lock.release()

latch = CountDownLatch(max_players)

# global variables
sync_lock = Lock()
players = []
current_player_id = 0

def send_json(player_id, json_object):
    message = json.dumps(json_object)
    players[player_id]["socket"].send(str(len(message)) + ":" + message)

def receive_json(player_id):
    message_length, message = players[player_id]["socket"].receive().split(":", 1)
    return json.loads(message)

def main_game():
    global players
    global sync_lock

    print("waiting for " + str(games[selected_game]) + " players")
    #latch.await()
    while(len(players) < max_players):
        gevent.sleep(1)

    print("game started")

    # Setup protocol
    for i in six.moves.range(max_players):
        setup = {
            "punter" : players[i]["punter_id"],
            "punters" : max_players,
            "map" : map_data
        }

        # only for non-web players
        if (players[i]["web_player"] != True):
            players[i]["to_client_queue"].put(setup)
            ready = players[i]["from_client_queue"].get()
            assert(ready["ready"] == players[i]["punter_id"])
        else:
            send_json(i, setup)

    # play loop

    moves = []
    num_moves = 0
    max_num_moves = len(map_data["sites"])
    seen_edges = {}

    for i in six.moves.range(max_players):
        moves.append({"pass" : {"punter" : i}})

    while(True):
        print("")
        print("###### GAME LOOP #########")
        print("")
        for i in six.moves.range(max_players):
            print("#######")
            print("Move " + str(num_moves + 1) + " of " + str(max_num_moves))
            print("#######")
            print("")

            move = {"move" : { "moves" : moves }}
            print("Sending move: " + players[i]["name"] + " " + json.dumps(move))
            players[i]["to_client_queue"].put(move)
            move = players[i]["from_client_queue"].get()
            # TODO validate move
            # Remove state from the move since we shouldn't be sending it back to the client
            move.pop('state', None)

            # Test for pass

            if "pass" in move :
                print("Player " + players[i]["name"] + " passed!")
            else :
                # Check that this move is good
                small = move["claim"]["source"]
                big = move["claim"]["target"]
                if small > big:
                    temp = small
                    small = big
                    big = small
                if (small, big) in seen_edges :
                    print("####################")
                    print("####################")
                    print("DUPLICATE")
                    print("####################")
                    print("####################")
                else:
                    seen_edges[(small, big)] = True

            print("Received moved: " + players[i]["name"] + " " + json.dumps(move))
            moves.append(move)
            moves = moves[1:]
            print("")

            num_moves += 1
            if (num_moves >= max_num_moves):
                break

        if (num_moves >= max_num_moves):
            break

    # Send the end game

    scores = []
    for i in six.moves.range(max_players):
        scores.append({"punter" : players[i]["punter_id"], "score" : 100 })

    for i in six.moves.range(max_players):
        end_move = { "stop" : { "moves" : moves, "scores" : scores } }
        players[i]["to_client_queue"].put(end_move)

    print("#########")
    print("GAME OVER")
    print("#########")

@sockets.route('/')
def echo_socket(ws):
    global current_player_id
    global players
    global sync_lock
    global end_lock
    to_client_queue = gevent.queue.Queue()
    from_client_queue = gevent.queue.Queue()

    with sync_lock:
        player_id = current_player_id
        current_player_id = current_player_id + 1

    while not ws.closed:
        # if this is json "me", it's a client, otherwise its a JS client
        message = ws.receive()
        try:
            message_json = json.load(message)
            print("Real client " + message_json)
            send({"you" : message_json["me"]})
        except Exception as e:
            # JS client
            host, port, name = message.split(":")
            with sync_lock:
                player = {
                    "web_player" : True,
                    "host" : host,
                    "port" : port,
                    "name" : name,
                    "punter_id" : player_id,
                    "socket" : ws,
                    "to_client_queue" : to_client_queue,
                    "from_client_queue" : from_client_queue
                    }
                players.append(player)

            print("JS client - host: " + host + " port: " + port + " name: " + name)
            ws.send(message)

        #latch.count_down()

        # Loop the queue
        for json_object in to_client_queue:
            send_json(player_id, json_object)
            response = receive_json(player_id)
            from_client_queue.put(response)

# Start the bg thread
gevent.spawn(main_game)
#game_thread = threading.Thread(target=main_game)
#game_thread.start()

# The TCP part

def receive_tcp_json(player_id):
    raw = players[player_id]["socket_reader"].readline()
    n, message = raw.split(":", 1)
    return json.loads(message)

def send_tcp_json(player_id, json_object):
    message = json.dumps(json_object)
    players[player_id]["socket"].sendall(str(len(message)) + ":" + message)

def tcp_handler(socket, address):
    global current_player_id

    to_client_queue = gevent.queue.Queue()
    from_client_queue = gevent.queue.Queue()

    with sync_lock:
        player_id = current_player_id
        current_player_id = current_player_id + 1
    socket_reader = socket.makefile(mode='rb')

    n, message = socket_reader.readline().split(":", 1)
    setup_json = json.loads(message)
    player = {
        "web_player" : False,
        "name" : setup_json["me"],
        "punter_id" : player_id,
        "socket" : socket,
        "socket_reader" : socket_reader,
        "to_client_queue" : to_client_queue,
        "from_client_queue" : from_client_queue
    }
    with sync_lock:
        players.append(player)

    print("Java client " + setup_json["me"])

    send_tcp_json(player_id, {"you" : setup_json["me"]})

    for json_object in to_client_queue:
        send_tcp_json(player_id, json_object)
        response = receive_tcp_json(player_id)
        # print("read " + json.dumps(response))
        from_client_queue.put(response)

def tcp_server():
    # to make the server use SSL, pass certfile and keyfile arguments to the constructor
    server = gevent.server.StreamServer(('', 9000), tcp_handler)
    # to start the server asynchronously, use its start() method;
    # we use blocking serve_forever() here because we have no other jobs
    server.serve_forever()

gevent.spawn(tcp_server)

@app.route('/')
def hello():
    return 'Hello World!'

if __name__ == "__main__":
    from gevent import pywsgi
    from geventwebsocket.handler import WebSocketHandler
    server = pywsgi.WSGIServer(('', 5000), app, handler_class=WebSocketHandler)
    server.serve_forever()
