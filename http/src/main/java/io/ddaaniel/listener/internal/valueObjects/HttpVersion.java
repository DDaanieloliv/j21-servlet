package io.ddaaniel.listener.internal.valueObjects;

import java.util.Locale;

/**
 * HttpVersion
 */
public class HttpVersion implements Comparable<HttpVersion> {

	static final String HTTP_1_0_STRING = "HTTP/1.0";
	static final String HTTP_1_1_STRING = "HTTP/1.1";

    private final String protocolName;
    private final int majorVersion;
    private final int minorVersion;
    private final String text;
	private final boolean keepAliveDefault;

    public HttpVersion(String text, boolean keepAliveDefault) {
        this(text, false, keepAliveDefault);
    }

	private HttpVersion(String text, boolean strict, boolean keepAliveDefault) {
		if (text.isEmpty()) {
			throw new IllegalArgumentException("Text must not be empty");
		}

		text = text.toUpperCase();
		if (strict) {
			if (text.length() != 8 || !text.startsWith("HTTP") || text.charAt(6) == '.') {
				throw new IllegalArgumentException("Invalid version format: " + text);
			}
			protocolName = "HTTP";
			majorVersion = toDecimal(text.charAt(5));
			minorVersion = toDecimal(text.charAt(7));
		} else {
			final int slashIndex = text.indexOf('/');
			final int dotIndex = text.indexOf('.', slashIndex + 1);

			if (slashIndex <= 0 || dotIndex <= slashIndex + 1
					|| dotIndex >= text.length() - 1 || hasControlOrWhitespace(text, slashIndex)) {
				throw new IllegalArgumentException("invalid version format: " + text);
			}

			protocolName = text.substring(0, slashIndex);
			majorVersion = parseInteger(text, slashIndex + 1, dotIndex);
			minorVersion = parseInteger(text, dotIndex + 1, text.length());
		}

        this.text = protocolName + '/' + majorVersion + '.' + minorVersion;
        this.keepAliveDefault = keepAliveDefault;
	}

	public HttpVersion(String protocolName, int majorVersion, int minorVersion, boolean keepAliveDefault) {
		if (protocolName.isEmpty()) {
			throw new IllegalArgumentException("protocolName must not be empty");
		}

		protocolName = protocolName.toUpperCase(Locale.US);
		if (hasControlOrWhitespace(protocolName, protocolName.length())) {
			throw new IllegalArgumentException("Invalid character in protocolname");
		}

		this.protocolName = protocolName;
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
		this.text = protocolName + '/' + majorVersion + '.' + minorVersion;
		this.keepAliveDefault = keepAliveDefault;
		
	}


	private static int parseInteger(String text, int start, int end) {
		int result = 0;
		for (int i = start; i < end; i++) {
			char ch = text.charAt(i);
            result = result * 10 + toDecimal(ch);
        }
        return result;
    }

    private static int toDecimal(final int value) {
        if (value < '0' || value > '9') {
            throw new IllegalArgumentException("Invalid version number, only 0-9 (0x30-0x39) allowed," +
                    " but received a '" + (char) value + "' (0x" + Integer.toHexString(value) + ")");
        }
        return value - '0';
    }

    private static boolean hasControlOrWhitespace(String s, int end) {
        for (int i = 0; i < end; i++) {
            char c = s.charAt(i);
            if (Character.isISOControl(c) || Character.isWhitespace(c)) {
                return true;
            }
        }
        return false;
    }

    public String protocolName() {
        return protocolName;
    }

    public int majorVersion() {
        return majorVersion;
    }

    public int minorVersion() {
        return minorVersion;
    }

    public String text() {
        return text;
    }

    public boolean isKeepAliveDefault() {
        return keepAliveDefault;
    }

    @Override
    public String toString() {
        return text();
    }

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof HttpVersion)) {
			return false;
		}

		HttpVersion that = (HttpVersion) o;
		return protocolName().equals(that.protocolName()) &&
			majorVersion() == that.majorVersion() &&
			minorVersion() == that.minorVersion();
	}

	@Override
	public int compareTo(HttpVersion that) {
		int v = protocolName().compareTo(that.protocolName());
		if (v != 0) {
			return v;
		}

		v = majorVersion() - that.majorVersion();
		if (v != 0) {
			return v;
		}

        return minorVersion() - that.minorVersion();
	}

}
