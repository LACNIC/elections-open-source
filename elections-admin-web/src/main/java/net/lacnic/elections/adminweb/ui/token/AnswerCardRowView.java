package net.lacnic.elections.adminweb.ui.token;

public interface AnswerCardRowView {

	String getQuestion();

	String getAnswer();

	boolean isAnswered();

	boolean isAccepted();

	String getDescription();
}
