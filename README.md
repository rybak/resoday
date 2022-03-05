![Resoday icon](src/main/resources/dev/andrybak/resoday/gui/resoday-icon.png)

Resoday – habit tracking calendar
=================================

This application emulates the [Every Day Calendar][Simone] by Simone Giertz 
shown in [Veritasium video _Why Most Resolutions Fail &amp; How To
Succeed_][Veritasium].

![Resoday screenshot](screenshot.png)

The app is in usable state, but effort is required to make a build for yourself.

Building
--------

Run in a terminal:

    $ ./gradlew clean release

After that, you can launch from file `build/distributions/resoday-release.jar`.

Requires Java 14 or newer.

Usage
-----

Build `resoday-release.jar` and launch it.  If your Java installation is not
integrated into operating system and/or desktop environment to automatically
launch `*.jar` files, use:

    $ java -jar resoday-release.jar

License
-------

Resoday source code is distributed under GNU General Public License version 3.
See `LICENSE.txt` for details.

TODO
----
[TODO list in Taskell (Markdown-like) format][TODO]

[Simone]: http://www.simonegiertz.com/every-day-calendar
[Veritasium]: https://www.youtube.com/watch?v=Pm9CQn07OjU&t=4m26s
[TODO]: https://github.com/rybak/resoday/blob/todo/TODO.md
