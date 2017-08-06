# ICFP2017

2017 ICFP Programming Contest at https://icfpcontest2017.github.io/

## Issues and things needed to be done

Please use the issues tab for known actions that need work to ensure we have a good feel for ongoing work.

## How to build

```bash
./gradlew clean build shadowJar 
```
## How to run in simulated offline mode

```bash
cat sampleSetupInput.json | java -jar build/libs/ICFP2017-1.0-SNAPSHOT-all.jar 
```

## How to run in online mode

```bash
java -jar build/libs/ICFP2017-1.0-SNAPSHOT-all.jar -mode=online -host=punter.inf.ed.ac.uk -port=9004 
```

## Capturing a log

```bash
java -jar build/libs/ICFP2017-1.0-SNAPSHOT-all.jar -mode=online -host=punter.inf.ed.ac.uk -port=9009 -capture
```

This will place the log in stderr.  Using the following:

```bash
java -jar build/libs/ICFP2017-1.0-SNAPSHOT-all.jar -mode=online -host=punter.inf.ed.ac.uk -port=9009 -capture &> capture.txt
```

## Replaying a log

Show replayer/index.html in a browser, paste the log in the replay information box and press the replay button.

## Overview notes

![Notes](Notes.png)
