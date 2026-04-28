package com.winwithpickr.twitter

import com.winwithpickr.core.models.Tier
import com.winwithpickr.core.models.TierConfig
import com.winwithpickr.twitter.models.XUser
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object ReplyFormatter {

    fun format(
        winners: List<XUser>,
        poolSize: Int,
        seed: String,
        tierConfig: TierConfig,
        giveawayId: String,
        followHostPartial: Boolean,
        appBaseUrl: String,
    ): String = buildString {
        if (winners.size == 1) {
            appendLine("\uD83C\uDF89 Winner: @${winners[0].username}")
        } else {
            appendLine("\uD83C\uDF89 Winners (${winners.size}):")
            winners.forEachIndexed { i, w -> appendLine("${i + 1}. @${w.username}") }
        }
        appendLine()
        val partialNote = if (followHostPartial) " \u00B7 \u26A0\uFE0F follower check partial" else ""
        appendLine("Pool: ${poolSize.fmt()} \u00B7 Seed: $seed$partialNote")
        append("\uD83D\uDD17 $appBaseUrl/r/$giveawayId")
        if (tierConfig.watermark) {
            appendLine()
            append("Powered by @winwithpickr")
        }
    }

    fun challengeConfirmReply(handle: String) =
        "\uD83E\uDDE9 Challenge is live @$handle!\n\n" +
        "Reply with your answer. When you reveal the correct one " +
        "(e.g. \"the answer is hodl\"), I'll draw a winner from all correct entries.\n\n" +
        "Follow @winwithpickr so you don't miss the DM setup \u2709\uFE0F"

    fun formatChallengeWinner(
        winners: List<XUser>,
        poolSize: Int,
        correctCount: Int,
        answer: String,
        seed: String,
        tierConfig: TierConfig,
        giveawayId: String,
        appBaseUrl: String,
    ): String {
        val truncAnswer = if (answer.length > 60) answer.take(57) + "..." else answer
        val link = "\uD83D\uDD17 $appBaseUrl/r/$giveawayId"
        val watermark = if (tierConfig.watermark) "\nPowered by @winwithpickr" else ""

        return buildString {
            appendLine("\uD83E\uDDE9 Pickr Challenge result")
            appendLine()
            appendLine("Answer: $truncAnswer")
            appendLine("$correctCount correct out of ${poolSize.fmt()} entries")
            appendLine()
            if (winners.size == 1) {
                appendLine("\uD83C\uDFC6 Winner: @${winners[0].username}")
                if (correctCount > 1) {
                    appendLine("Selected from $correctCount correct entries via verifiable random seed")
                }
            } else {
                appendLine("\uD83C\uDFC6 Winners (${winners.size}):")
                val shown = winners.take(3)
                shown.forEachIndexed { i, w -> appendLine("${i + 1}. @${w.username}") }
                if (winners.size > 3) {
                    appendLine("+ ${winners.size - 3} more \u2014 see full list below")
                }
                if (correctCount > winners.size) {
                    appendLine("Selected from $correctCount correct entries via verifiable random seed")
                }
            }
            appendLine()
            append(link)
            append(watermark)
        }.let { reply ->
            if (reply.length <= 280) reply else {
                buildString {
                    appendLine("\uD83E\uDDE9 Pickr Challenge result")
                    appendLine()
                    appendLine("\uD83C\uDFC6 Winner: @${winners[0].username}")
                    appendLine()
                    append(link)
                    append(watermark)
                }
            }
        }
    }

    fun predictConfirmReply(handle: String) =
        "\uD83D\uDD2E Got it @$handle! Watching for predictions.\n\n" +
        "When you're ready, reply with the answer (e.g. \"the answer is UConn 82 Michigan 75\") " +
        "and I'll score every prediction and pick a winner.\n\n" +
        "Follow @winwithpickr so you don't miss the DM setup \u2709\uFE0F"

    fun formatPredictWinner(
        winners: List<XUser>,
        poolSize: Int,
        topScoreCount: Int,
        answer: String,
        maxScore: Int,
        seed: String,
        tierConfig: TierConfig,
        giveawayId: String,
        appBaseUrl: String,
        /** Optional early-pick bonus applied to the top winner's score; renders inline only when > 0. */
        winnerBonus: Double? = null,
    ): String {
        val truncAnswer = if (answer.length > 60) answer.take(57) + "..." else answer
        val link = "\uD83D\uDD17 $appBaseUrl/r/$giveawayId"
        val watermark = if (tierConfig.watermark) "\nPowered by @winwithpickr" else ""
        val label = if (maxScore == 100) "perfect prediction" else "top prediction"
        val bonusNote = winnerBonus?.takeIf { it > 0.0 }?.let { " +${"%.1f".format(it)} early" } ?: ""

        return buildString {
            appendLine("\uD83D\uDD2E Pickr Predict result")
            appendLine()
            appendLine("Answer: $truncAnswer")
            appendLine("$topScoreCount $label${if (topScoreCount != 1) "s" else ""} out of ${poolSize.fmt()} entries")
            appendLine()
            if (winners.size == 1) {
                appendLine("\uD83C\uDFC6 Winner: @${winners[0].username} (score: $maxScore/100$bonusNote)")
                if (topScoreCount > 1) {
                    appendLine("Tied with ${topScoreCount - 1} other${if (topScoreCount > 2) "s" else ""} \u2014 broken by verifiable random seed")
                }
            } else {
                appendLine("\uD83C\uDFC6 Winners (${winners.size}):")
                // List up to 3 winners inline, rest on verification page
                val shown = winners.take(3)
                shown.forEachIndexed { i, w -> appendLine("${i + 1}. @${w.username} (score: $maxScore/100)") }
                if (winners.size > 3) {
                    appendLine("+ ${winners.size - 3} more \u2014 see full list below")
                }
                if (topScoreCount > winners.size) {
                    appendLine("Selected from $topScoreCount tied entries via verifiable random seed")
                }
            }
            appendLine()
            append(link)
            append(watermark)
        }.let { reply ->
            // Hard cap: truncate middle section if still over 280
            if (reply.length <= 280) reply else {
                buildString {
                    appendLine("\uD83D\uDD2E Pickr Predict result")
                    appendLine()
                    appendLine("\uD83C\uDFC6 Winner: @${winners[0].username} (score: $maxScore/100$bonusNote)")
                    appendLine()
                    append(link)
                    append(watermark)
                }
            }
        }
    }

    fun startConfirmReply(handle: String) =
        "Got it @$handle! Watching this giveaway \uD83D\uDC40\n\n" +
        "Reply to your tweet (or quote-RT) with \"picking a winner\" " +
        "when ready \u2014 I'll draw automatically.\n\n" +
        "Follow @winwithpickr so you don't miss the DM setup \u2709\uFE0F"

    fun alreadyStartedReply(handle: String) =
        "Hey @$handle \u2014 already watching this giveaway! " +
        "Reply with \"picking a winner\" when you're ready."

    fun giveawayExpiredReply(handle: String) =
        "Hey @$handle \u2014 this giveaway's 7-day window closed (X API limit). " +
        "I can no longer fetch replies.\n\nFor future giveaways tag me within 7 days."

    fun scheduledConfirmReply(handle: String, delayMs: Long?): String {
        val dur = delayMs?.let { if (it < 86_400_000) "${it / 3_600_000}h" else "${it / 86_400_000}d" } ?: "the scheduled time"
        return "Got it @$handle! I'll pick a winner in $dur \u23F1"
    }

    fun noEntriesReply(handle: String) =
        "Hey @$handle \u2014 no eligible entries found. " +
        "Check that the conditions match how participants entered."

    fun monthlyLimitReply(handle: String, limit: Int, tier: Tier, overageRate: Int?, xId: String, tweetId: String, secret: String, baseUrl: String): String {
        if (tier == Tier.FREE) {
            return "Hey @$handle \u2014 you've used all $limit free picks this month.\n\n" +
                "Upgrade to Pro ($19/mo, 10 picks included):\n${upgradeUrl(xId, tweetId, "pro", secret, baseUrl)}"
        }
        val rateFmt = overageRate?.let { "$${it / 100}.${"%02d".format(it % 100)}" } ?: "$0.50"
        return "Hey @$handle \u2014 you've used your $limit included picks this month.\n\n" +
            "Extra picks are $rateFmt each. Pick will proceed and the overage " +
            "will appear on your next invoice."
    }

    fun followerGateReply(handle: String, tweetId: String, xId: String, secret: String, baseUrl: String) =
        "Hey @$handle \u2014 follower verification is a paid add-on (+$1/pick).\n\n" +
        "Upgrade to Pro to unlock: ${upgradeUrl(xId, tweetId, "pro", secret, baseUrl)}\n\n" +
        "To pick without it: \"@winwithpickr pick from replies\""

    fun upgradeUrl(xId: String, tweetId: String, plan: String, secret: String, baseUrl: String): String {
        val payload = "$xId:$tweetId:$plan"
        val sig = hmacSha256(payload, secret)
        return "$baseUrl/upgrade?xid=$xId&ref=$tweetId&plan=$plan&sig=$sig"
    }

    private fun hmacSha256(data: String, secret: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        return Base64.getUrlEncoder().withoutPadding()
            .encodeToString(mac.doFinal(data.toByteArray(Charsets.UTF_8)))
    }

    fun followAccountsGateReply(handle: String, tweetId: String, xId: String, secret: String, baseUrl: String) =
        "Hey @$handle \u2014 \"must follow\" account checks are a paid add-on (+$0.50/pick per account).\n\n" +
        "Upgrade to Pro to unlock: ${upgradeUrl(xId, tweetId, "pro", secret, baseUrl)}\n\n" +
        "To pick without it: \"@winwithpickr pick from replies\""

    fun fraudFilterGateReply(handle: String, tweetId: String, xId: String, secret: String, baseUrl: String) =
        "Hey @$handle \u2014 fraud filtering (account age / follower minimum) requires Business.\n\n" +
        "Upgrade: ${upgradeUrl(xId, tweetId, "business", secret, baseUrl)}\n\n" +
        "To pick without it: \"@winwithpickr pick from replies\""

    fun scheduledPicksGateReply(handle: String, tweetId: String, xId: String, secret: String, baseUrl: String) =
        "Hey @$handle \u2014 scheduled picks require Business.\n\n" +
        "Upgrade: ${upgradeUrl(xId, tweetId, "business", secret, baseUrl)}\n\n" +
        "Tip: use \"@winwithpickr start\" to pick when you're ready instead."

    fun tweetTooOldReply(handle: String, ageDays: Int) =
        "Hey @$handle \u2014 replies only available within 7 days of the original tweet " +
        "(X API limit). This tweet is $ageDays days old."

    fun partialFollowerNote(appBaseUrl: String, giveawayId: String) =
        "Note: follower check covered first 15,000 followers due to API limits. " +
        "Details: $appBaseUrl/r/$giveawayId"

    private fun Int.fmt() = "%,d".format(this)
}
