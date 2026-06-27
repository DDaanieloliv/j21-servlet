package io.ddaaniel.internal.parser.reader.state;

/**
 * ParseState
 */
public enum ParsingState {
	STATE_INIT,
	STATE_HEADERS,
	STATE_BODY,
	STATE_DONE,
	STATE_ERROR;
}
