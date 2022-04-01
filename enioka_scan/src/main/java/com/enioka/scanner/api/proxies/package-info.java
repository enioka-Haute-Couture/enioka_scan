/**
 * Callback proxies serve the purpose of centralizing jumps to UI-thread by wrapping their respective callbacks and calling the UI-thread.
 * This way, regular callbacks and other methods do not have to worry about handling this, handlers just need to be wrapped before being passed as parameters.
 */
package com.enioka.scanner.api.proxies;