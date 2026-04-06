# pickr-twitter

X/Twitter SDK for [@winwithpickr](https://x.com/winwithpickr) — command parsing, pool building, reply formatting, and OAuth 1.0a signing for the X API.

Open-source, MIT-licensed. Kotlin Multiplatform (JVM + JS).

Part of the [winwithpickr](https://github.com/winwithpickr) ecosystem:
- [pickr-engine](https://github.com/winwithpickr/pickr-engine) — platform-agnostic verification core
- **pickr-twitter** — this repo
- [pickr-telegram](https://github.com/winwithpickr/pickr-telegram) — Telegram SDK

## What this library does

pickr-twitter handles everything specific to X/Twitter:

- **Command parsing** — `@winwithpickr pick 3 from replies+retweets followers only` → structured `ParsedCommand`
- **Pool building** — assembles entry pool from replies, retweets, and quote tweets via `XPoolBuilder`
- **Follower filtering** — `FollowHostFilter`, `FollowAccountsFilter` for paid-tier gating
- **Fraud filtering** — account age + follower count thresholds (Business tier)
- **Reply formatting** — winner announcements, gate messages, upgrade prompts
- **OAuth 1.0a signing** — HMAC-SHA1 request signing for X API v2
- **JS exports** — `verifyPick()` and `parseCommand()` for browser verification pages

## Install

### Gradle (JVM / Kotlin Multiplatform)

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/winwithpickr/*")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("com.winwithpickr:twitter:0.3.0")
}
```

### npm

```bash
npm install @winwithpickr/twitter
```

The npm package exports `verifyPick()` and `parseCommand()` for browser use (verification pages, command preview).

## Modules

| Module | Target | Description |
|---|---|---|
| `CommandParser` | common | Parses `@winwithpickr` mention text into `ParsedCommand` |
| `XPoolBuilder` | common | Assembles entry pool pipeline from conditions + tier config |
| `XDataSource` | common | Interface for X API data access (replies, retweets, followers) |
| `ReplyFormatter` | JVM | Result tweet formatting, signed upgrade URLs |
| `OAuth1Signer` | JVM | HMAC-SHA1 OAuth 1.0a signing for X API v2 |

### Sources (common)

| Source | Description |
|---|---|
| `ReplySource` | Fetches direct replies to the giveaway tweet |
| `RetweetSource` | Fetches retweet authors |
| `QuoteTweetSource` | Fetches quote tweet authors with optional text matching |

### Filters (common)

| Filter | Tier | Description |
|---|---|---|
| `FollowHostFilter` | Pro+ | Requires entrants follow the giveaway host |
| `FollowAccountsFilter` | Pro+ | Requires entrants follow specified accounts |
| `FraudFilter` | Business | Account age + follower count thresholds |
| `HashtagFilter` | All | Requires specific hashtag in reply text |
| `MinTagsFilter` | All | Requires tagging N friends in reply |
| `QuoteTextFilter` | All | Requires specific text in quote tweets |

### Models (common)

| Model | Description |
|---|---|
| `ParsedCommand` | Parsed command: winner count, conditions, trigger mode |
| `EntryConditions` | Reply, retweet, like, followHost, followAccounts, fraud thresholds |
| `XUser` | X user identity (id, username, public metrics) |

## Commands

The bot understands both structured commands and natural language. On X, mentions go through the LLM parser first ([pickr-anthropic](https://github.com/winwithpickr/pickr-anthropic)) and fall back to the regex `CommandParser` in this library. The regex parser is also compiled to JavaScript for the live command tester on the website.

### Natural language (via LLM)

| Example | What it extracts |
|---|---|
| `@winwithpickr start a giveaway, pick 3 winners from people who replied and retweeted, must be following me` | 3 winners, reply + retweet, follower check, watch mode |
| `@winwithpickr pick a winner from the retweets, followers only` | 1 winner from retweeters, follower check |
| `@winwithpickr choose 5 winners in 24 hours from replies` | 5 winners, scheduled 24h, from replies |

### Structured commands (regex)

Reply to any giveaway tweet with:

| Command | What it does |
|---|---|
| `@winwithpickr pick` | Pick 1 winner from replies |
| `@winwithpickr pick 3` | Pick 3 winners |
| `@winwithpickr pick from replies+retweets` | Pick from both pools (intersection) |
| `@winwithpickr pick followers only` | Only pick from host's followers (Pro+) |
| `@winwithpickr pick follow:@acct1,@acct2` | Must follow specified accounts (Pro+) |
| `@winwithpickr watch` | Watch giveaway, pick when host triggers |
| `@winwithpickr pick in 2h` | Scheduled pick (Business) |

## JavaScript API

```javascript
import { verifyPick, parseCommand } from "@winwithpickr/twitter";

// Verify a pick result
const result = verifyPick(seed, poolIds, winnerCount);
// result.winners  — computed winner IDs
// result.poolHash — computed SHA-256 pool hash

// Parse a command
const cmd = parseCommand("@winwithpickr pick 3 from replies+retweets");
// cmd.valid, cmd.winners, cmd.mode, cmd.reply, cmd.retweet, ...
```

## Building

```bash
# Run all tests
./gradlew allTests

# Build JS bundle (pickr-parser.js)
./gradlew jsBrowserProductionWebpack

# Publish to Maven local
./gradlew publishToMavenLocal
```

## License

MIT — see [LICENSE](LICENSE)
