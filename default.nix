{ pkgs ? import <nixpkgs> {} }:

(pkgs.buildFHSUserEnv {
  name = "harry-plotter";
  targetPkgs = pkgs: (with pkgs;
    [  openjdk16
       gradle
       chia
    ]);
  runScript = "./gradlew run";
}).env