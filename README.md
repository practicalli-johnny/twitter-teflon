# twitter-teflon

Delete all your low-score tweets, retweets, and likes.

Before using this app, you are advised to [back up your twitter archive](https://twitter.com/settings/account).

You must create `config.edn` containing

```clojure
{:twitter_consumer_key        "..."
 :twitter_consumer_secret     "..."
 :twitter_access_token        "..."
 :twitter_access_token_secret "..."
 :twitter_username            "..."}
```

as per your App available at [apps.twitter.com](https://apps.twitter.com).

This app intentionally does not use [clojure-twitter](https://github.com/mattrepl/clojure-twitter/) as it is an exercise in learning Clojure.
