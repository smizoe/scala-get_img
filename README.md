scala-get_img
=============

a program to collect images using Bing API.

build
-----

First you need to clone this repositiory. In the cloned directory, type

    $ sbt assembly

and a jar file is created in 'target' directory.
(You need to edit src/test/resources/reference.conf and set a correct api key for bing search API
 so that tests finish successfully.)


how to run
----------

    scala path/to/jar --apikey 'your api key' --keyword kitten

and you will get a bunch of images of cute kittens.
