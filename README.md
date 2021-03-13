# finford-ui
Details Go Here...

### Make sure you have shadow-cljs installed
```
npm install -g shadow-cljs
```

## Development

### Running locally

* Start application `clojure -Adev`
* App will be running at `http://localhost:9876/#/`
* Connect to repl `9001` or whatever is in shadow.edn
* Run `(shadow/repl :app)` from `script/repl.clj`

### Run Tests

``` sh
clj -A:test
```

Then visit:

`http://localhost:8021/`

## Sass Stylesheets (.scss)

Stylesheet source files are located in `/src/sass` and are written in
[Sass](http://sass-lang.com/) `.scss` syntax.

Stylesheet Development:

```
cd src/sass
yarn install

# Compile CSS once, while in /sass dir
yarn css

# Compile and watch, while in /sass dir
yarn watch:css
```

### Add NPM Modules

Add necessary modules to `package.json`

`$ yarn install`

### Debuger In the Browser
http://localhost:9876/#/debug

### Debug In Production
Set finford-ui-debug-override localStorage key to true

### Package For Deployment

* `cd src/sass; yarn css`
* `clojure -Amin`
* The app will be in `resources/public/`


