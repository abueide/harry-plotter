{ pkgs ? import <nixpkgs> {} }:
  pkgs.mkShell {
    # nativeBuildInputs is usually what you want -- tools you need to run
    nativeBuildInputs = with pkgs; [
        jdk16 chia
        ((pkgs.gradleGen.override { java = jdk16; }).gradle_latest)
    ];
    shellHook = ''
        export JAVA_HOME=${pkgs.jdk16.home}
    '';
}