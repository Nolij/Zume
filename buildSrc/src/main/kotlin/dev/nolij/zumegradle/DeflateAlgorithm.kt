package dev.nolij.zumegradle

import xyz.wagyourtail.unimined.util.capitalized

enum class DeflateAlgorithm(val id: Int) {
	
	/**
	 * Entries are stored without compression
	 */
	STORE(0),

	/**
	 * Entries are stored with Zlib
	 */
	FAST(1),

	/**
	 * Entries are stored with libdeflate
	 */
	NORMAL(2),
	
	/**
	 * Entries are stored with 7zip
	 */
	EXTRA(3),
	
	/**
	 * Entries are stored with Zopfli
	 */
	INSANE(4);

	override fun toString() = name.lowercase().capitalized()
}