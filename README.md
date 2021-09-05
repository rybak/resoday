![Resoday icon](src/main/resources/dev/andrybak/resoday/gui/resoday-icon.png)

Resoday – habit tracking calendar
=================================

This application emulates the [Every Day Calendar][Simone] by Simone Giertz 
shown in [Veritasium video _Why Most Resolutions Fail &amp; How To
Succeed_][Veritasium].

The app is in usable state, but effort is required to make a build for yourself.

Building
--------

Run in a terminal:

    ./gradlew distZip

Artifact will be located in `build/distributions`.  Unpack the zip to any place
you want.  The script to start the app will be unpacked into `bin` subfolder.

Requires Java 14 or newer.

Running
-------

    ./bin/resoday

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
