# Htwg Scala Minesweeper Project
=====================================================


This is a project developped by [Steve Madoerin](https://github.com/SteveMadoerin) to create the game Minesweeper written in Scala used in the
class Software Engineering at the University of Applied Science HTWG Konstanz.


[![Coverage Status](https://coveralls.io/repos/github/SteveMadoerin/minesweeperpublic/badge.svg?branch=main)](https://coveralls.io/github/SteveMadoerin/minesweeperpublic?branch=main)
                                                [![Scala CI](https://github.com/SteveMadoerin/minesweeperpublic/actions/workflows/scala.yml/badge.svg?branch=development&event=push)](https://github.com/SteveMadoerin/minesweeperpublic/actions/workflows/scala.yml)

![Minesweeper_Steve_Madoerin](https://github.com/SteveMadoerin/minesweeper/assets/115166447/9b831e76-6268-4ecd-a35e-a2448302f2f9)

# How To Play

Minesweeper is a calculable logic and thinking game. The number fields indicate how many bombs are located within a radius of eight fields around the number field. If you suspect a bomb field, place a red flag as a marker. Uncover all the squares without activating a bomb to win the game.

When using the Graphical User Interface:
- `leftklick` to open a Field
- `rightklick` to flag or unflag a Field

When using the Text User Interface:

`<command><xy>`
- `o0108` opens the field at location x: 01 and y: 08
- `f0201` put a flag on the field at x: 02 and y: 01
- `u0201` put a unflags the field at x: 02 and y: 01

`<command>`

- `h` help
- `z` undo move
- `y` redo move
- `q` quit
- `h` help
- `r` reveals the field - cheating
- `s` save Game
- `l` load Game

# Using Docker

- `docker build -t minesweeper-image .` to build image
- `docker run -it --rm -v /tmp/.X11-unix:/tmp/.X11-unix --device /dev/dri -p 8080:8080 --privileged -v :/minesweeper minesweeper-image` to run image



