{ pkgs ? import <nixpkgs> {} }:

(pkgs.buildFHSUserEnv {
  name = "harry-plotter";
  targetPkgs = pkgs: (with pkgs;
    [  openjdk16
    ]);
  runScript = "./gradlew run";
}).env