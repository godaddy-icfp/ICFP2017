# ICFP2017

2017 ICFP Programming Contest at https://icfpcontest2017.github.io/

## Issues and things needed to be done

Please use the issues tab for known actions that need work to ensure we have a good feel for ongoing work.

## How to build and run this

```bash
./gradlew clean build shadowJar
cat sampleSetupInput.json | java -jar build/libs/ICFP2017-1.0-SNAPSHOT-all.jar 
```

## How to create an IntelliJ configuration file for input

The first parameter of the jar file startup will be converted to stdin to simplify test and debug of the system.  Create a configuration in IntelliJ/Eclipse with the build step as a precursor to obtain the shadow jar and then pass the file you are testing as the first parameter.

![2017-08-04_16-37-33](media/2017-08-04_16-37-33.png)
