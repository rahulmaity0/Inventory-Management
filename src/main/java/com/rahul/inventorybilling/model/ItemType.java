package com.rahul.inventorybilling.model;

/**
 * What kind of ornament a transaction was for.
 *
 * An enum is just a fixed list of allowed values. Because the field is typed
 * as ItemType and not String, nothing can ever put "neckl4ce" in the database
 * - Jackson rejects a bad value in the request before the service even runs.
 *
 * To add a kind later, add one line here. Nothing else has to change.
 */
public enum ItemType {
    NECKLACE,
    EARRINGS,
    BANGLES,
    CHAIN,
    RING,
    BRACELET,
    COIN_OR_BAR,
    OTHER
}
