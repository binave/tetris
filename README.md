# Tetris

A Tetris game with LAN multiplayer support.

Run with `--help` for usage information.


[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
- [简体中文](README.zh-CN.md)

## Licensing

This project is licensed under the Apache License, Version 2.0. See
[LICENSE](https://github.com/binave/tetris/blob/master/LICENSE) for the full
license text.


## Usage

```
usage: java -jar [jar_path] [option]

Options:
    --help,   -h
        Show this help message.

    --single, -1
        Classic mode.
        Use arrow keys to control the block.
        Up arrow rotates the block, Space drops the block instantly, Left Shift consumes SP to swap blocks.
        Press P to pause, ESC to quit the game.

    --dual,   -2
        Same-screen two-player mode.
        Player 1 (host) uses WASD for movement and rotation, Z for instant drop, Q consumes SP to swap blocks.
        Player 2 uses arrow keys for movement, / for instant drop, . (period) consumes SP to swap blocks.
        Space pauses the game, ESC quits.
        In two-player mode, the goal is no longer to score points, but to mess up your opponent and cause them to lose.

    --online-server,  -s  [[port]]
        LAN two-player mode (host).
        Must be started first. Optional listening port can be specified. After the client connects, press P to start; press P again during gameplay to pause.
        Controls are the same as in classic mode.
        Winning condition is the same as in two-player mode.

    --online-client,  -c  [ipv4] [[port]]
        LAN two-player mode (client).
        Requires the host's IP to start.
        Controls are the same as in classic mode. The client cannot control pause.
        Winning condition is the same as in two-player mode.
```

## Two-Player Mode Rules

| Lines Cleared | Effect |
|---------------|--------|
| 1 line | Only clears the line |
| 2 lines | Rewards SP points |
| 3 lines | Rewards SP points + transfers the 4th line to opponent |
| 4 lines | Rewards SP points + accelerates opponent's block fall |

- Left Shift consumes SP to swap blocks; this can also cancel the acceleration penalty
- Observing your opponent's status is crucial
- SP points have a storage cap and do not carry over to the next round, so use them frequently


## Features

- Supports AOT (Ahead-of-Time) compilation
