# twitter-teflon

Delete all your low-score tweets, retweets, and likes.

Before using this app, you are advised to [back up your twitter archive](https://twitter.com/settings/account).

You must create `config.clj` containing

```clojure
(def ^:const +twitter_consumer_key+ "...")
(def ^:const +twitter_consumer_secret+ "...")
(def ^:const +twitter_access_token+ "...")
(def ^:const +twitter_access_token_secret+ "...")
```

as per your App available at [apps.twitter.com](https://apps.twitter.com).

This app intentionally does not use [clojure-twitter](https://github.com/mattrepl/clojure-twitter/) as it is an exercise in learning Clojure.
