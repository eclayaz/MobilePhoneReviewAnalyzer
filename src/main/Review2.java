package main;

public class Review2 {
	String comment;
	String rating;
	String phone;
	String isAbout;
	
	public Review2(String comment, String rating, String phone,	String isAbout) {
		this.comment = comment;
		this.rating = rating;
		this.phone = phone;
		this.isAbout = isAbout;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getIsAbout() {
		return isAbout;
	}

	public void setIsAbout(String isAbout) {
		this.isAbout = isAbout;
	}
	
}
