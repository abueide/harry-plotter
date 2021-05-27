# Harry Plotter

You're a farmer, Harry!

Harry Plotter is an easy to use magical Chia plot manager for muggles! It works on Windows, MacOS, and Linux. It should work on ARM devices as well, but you'll have to compile from source (see below) which is also very easy.

Have a question or suggestion? Join the discord https://discord.gg/Uxh5ZqJVsS

![demo.png](wiki/demo.png)

## Support

I have spent a ton of time learning for & working on Harry Plotter, and covid has hit me pretty hard, so if you appreciate my work please consider sending a tip! In addition you can post your transaction ID on a github issue (feature, bug or setup question) and I'll work through them by order of tip amount. You can also feel free to shoot me an email. However: it is still a tip and I cannot guarantee I will be able to solve your problem satisfactorily in a reasonable amount of time.

Chia Address: xch1ktqlc03wpetne9e0e6frz497xhhx4yx08gzn63tvjwlrlg2cg2ksf7ls24

Eth Address: 0x039c1c74e3AaCCB74457880099F441f61C9c9bAc

BTC Address: bc1qlrujsyrspf587pj2ydrz9h0k5070wfzqwq3pxf

PayPal: andrew@abueide.com

## Installation

Download the installer for your platform here: https://github.com/abueide/harry-plotter/releases

## Usage

When you first open Harry Plotter, you may be prompted to locate your Chia executable and/or your chia config file. This will happen if you compiled from source (usually on linux) or if you installed chia to a nonstandard location.

If this happens, consult the Chia CLI wiki to find directory paths for your specific platform.

https://github.com/Chia-Network/chia-blockchain/wiki/CLI-Commands-Reference

After Harry Plotter starts up, it will automatically import your public keys (it does not read or touch your private keys, nor does it need access to them in order to do its job). If you'd like to use Harry Plotter on a machine without any imported keys, then simply click on the Add Key button and paste in your 2 public keys (only the farmer & pool public key is needed at this time).

Once you've imported your keys then simply fill out the info in the top right panel & click save to create your plot job. Create multiple plot jobs for each plot you want to make in parallel. If you want to stagger the plots, then create them in the order you want them to run, set the stagger timer at the bottom left, and then click start all.

Upon closing the application it will prompt you if you want to let your plot jobs finish or if you want to cancel them.

When stopping a plot job (or closing out of the application and not letting them finish), then Harry Plotter will attempt to clean up all the generated tmp files automatically.

You can also toggle dark/light mode by clicking the opposite colored button at the top right (an icon will be added eventually).

## Compiling

### Dependencies

[JDK 16](https://adoptopenjdk.net/?variant=openjdk16&jvmVariant=hotspot)

Ubuntu/Debian users: `sudo apt install openjdk-16-jdk`

### Windows
`git clone https://github.com/abueide/harry-plotter.git`

`cd harry-plotter`

`gradlew.bat run` --  runs the application

`gradlew.bat jpackage` --  Creates an installer for your current platform which can be found in harry-plotter/build/jpackage/

### MacOS/Linux

`git clone https://github.com/abueide/harry-plotter.git`

`cd harry-plotter`

`chmod +x gradlew`

`./gradlew run` --  runs the application

`./gradlew jpackage` --  Creates an installer for your current platform which can be found in harry-plotter/build/jpackage/

# Roadmap
[ ] Official Pooling Protocol Support

[ ] Magical auto plot tuning algorithm

[ ] Easy to use harvester setup on multiple machines

[ ] Logs & Harvester monitoring

[ ] Cool dashboard with stats

[ ] Alerts/Notifications when harvester or plotter is having problems

## Contributing

[See CONTRIBUTING.md](CONTRIBUTING.md)
