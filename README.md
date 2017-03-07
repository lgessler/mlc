# mlc

A tiny single-page application for showing points of linguistic interest on a map. Developed for [the Madison Language Capital project](http://mlc.lgessler.com). Still under development. Contact lukegessler@gmail.com with questions.

# Build Instructions

## Development Mode

### Compile css:

Compile css file once.

```
lein less once
```

Automatically recompile css file on change.

```
lein less auto
```

### Run application:

```
lein clean
lein figwheel dev
```

Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).

## Production Build


To compile clojurescript to javascript:

```
lein clean
lein cljsbuild once min
```
