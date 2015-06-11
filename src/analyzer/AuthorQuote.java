package analyzer;

public class AuthorQuote {
	private String _author = null;
	private String _quote = null;
	private String _description = null;

	public String getAuthor() {
		assert (_author != null);
		return _author;
	}

	public String getQuote() {
		assert (_quote != null);
		return _quote;
	}
	
	public String getDescription() {
		assert (_description != null);
		return _description;
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
}