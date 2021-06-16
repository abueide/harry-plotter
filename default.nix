{   mkDerivation, fetchFromGitHub,
    console ? false,
    softwareRender ? false
}:
mkDerivation rec {
  pname = "harry-plotter";
  version = "1.1.14";

  src = pkgs.fetchFromGitHub {
    owner = "abueide";
    repo = "harry-plotter";
    rev = version;
    sha256 = "1yycdzbgsz3jsd1jgcm49igdd2f5cvi30xfaij9ac9liyi4b5h5p";
  };

  buildInputs = with pkgs; [
    jdk
  ];

  configurePhase = ''
  '';

  buildPhase = ''
    ./gradlew jpackage
  '';

  installPhase = ''
        
  '';
}
