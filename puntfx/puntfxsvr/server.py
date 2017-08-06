from flask import Flask
from flask_sockets import Sockets
from threading import Lock
import threading
import json
import gevent
import gevent.lock

app = Flask(__name__)
sockets = Sockets(app)

games = {"sample.json" : 2}
selected_game = "sample.json"
max_players = games[selected_game]

with open(selected_game) as data_file:
    map_data = json.load(data_file)

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
    message_length, message = players[player_id]["socket"].receive().split(":")
    return json.load(message)

def main_game():
    global players
    global sync_lock

    print("waiting for " + str(games[selected_game]) + " players")
    latch.await()
    print("game started")

    # Setup protocol
    for i in xrange(max_players):
        setup_message = {
            "punter" : players[i]["punter_id"],
            "punters" : max_players,
            "map" : map_data
        }
        print("sending setup to " + str(i) + " " + json.dumps(setup_message))
        send_json(i, setup_message)

        # only for non-web players
        if (players[i]["web_player"] != True):
            message_json = receive_json(i)
            assert(message_json["ready"] == players[i]["punter_id"])

    # play loop

    while(True):
        moves = []
        for i in xrange(max_players):
            moves.append({"pass" : {"punter" : i}})

        for i in xrange(max_players):
            move = {"move" : { "moves" : moves }}
            send_json(i, move)
            response = receive_json(i)



    moves = []

@sockets.route('/')
def echo_socket(ws):
    print("Got connection")
    global current_player_id
    global players
    global sync_lock
    global end_lock
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
                    "punter_id" : current_player_id,
                    "socket" : ws
                    }
                players.append(player)
                current_player_id = current_player_id + 1

            print("JS client - host: " + host + " port: " + port + " name: " + name)
            ws.send(message)

        latch.count_down()

# Start the bg thread
#gevent.spawn(main_game)
game_thread = threading.Thread(target=main_game)
game_thread.start()

@app.route('/')
def hello():
    return 'Hello World!'

if __name__ == "__main__":
    from gevent import pywsgi
    from geventwebsocket.handler import WebSocketHandler
    server = pywsgi.WSGIServer(('', 5000), app, handler_class=WebSocketHandler)
    server.serve_forever()
