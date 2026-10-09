/*
 * Vendored from afollestad/easy-video-player at tag 0.3.0
 * (commit 8d6c36e5318d27662bb52d75f19e65aaa537eea0), licensed Apache 2.0 — see
 * LICENSE.md in this directory.
 *
 * The com.afollestad:easyvideoplayer:0.3.0 artifact only ever existed on jcenter,
 * which is shut down, and the upstream GitHub repo is gone, so the source is kept
 * here instead. Changes from upstream: android.support.* imports rewritten to
 * AndroidX (jetifier only rewrites dependencies, not project source).
 */
package com.afollestad.easyvideoplayer;

import android.net.Uri;

/**
 * @author Aidan Follestad (afollestad)
 */
public interface EasyVideoCallback {

    void onStarted(EasyVideoPlayer player);

    void onPaused(EasyVideoPlayer player);

    void onPreparing(EasyVideoPlayer player);

    void onPrepared(EasyVideoPlayer player);

    void onBuffering(int percent);

    void onError(EasyVideoPlayer player, Exception e);

    void onCompletion(EasyVideoPlayer player);

    void onRetry(EasyVideoPlayer player, Uri source);

    void onSubmit(EasyVideoPlayer player, Uri source);
}