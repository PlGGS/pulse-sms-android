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

/**
 * @author Aidan Follestad (afollestad)
 */
public interface EasyVideoProgressCallback {

    void onVideoProgressUpdate(int position, int duration);
}
