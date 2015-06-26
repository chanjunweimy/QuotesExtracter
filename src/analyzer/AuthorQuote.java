package analyzer;

public class AuthorQuote {
	private String _author = null;
	private String _quote = null;
	private String _description = null;
	private String _rule = null;

	public String getAuthor() {
		assert (_author != null);
		return _author;
	}

	public String getQuote() {
		assert (_quote != null);
		return _quote;
	}
	
	public String getDescription() {
		if (_description == null) {
			_description = "";
		}

		return _description;
	}
	
	public String getRule() {
		if (_rule == null) {
			_rule = "";
		}
		return _rule;
	}

	public void setAuthor(String author) {
		_author = author;
	}

	public void setQuote(String quote) {
		_quote = quote;
	}
	
	public void setDescription(String description) {
		_description = description;
	}

	public void setRule(String rule) {
		_rule = rule;
	}
}