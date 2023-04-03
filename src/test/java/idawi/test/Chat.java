package idawi.test;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import idawi.Component;
import idawi.Service;
import idawi.TypedInnerClassOperation;
import toools.gui.Swingable;

public class Chat extends Service implements Swingable {
	private final JPanel panel = new JPanel(new BorderLayout());
	JTextArea conversationPane = new JTextArea();

	public Chat(Component peer) {
		super(peer);
		JTextField textInput = new JTextField();
		panel.add(conversationPane, BorderLayout.CENTER);
		panel.add(textInput, BorderLayout.SOUTH);
		textInput.requestFocus();

		textInput.addActionListener(e -> {
			String text = textInput.getText().trim();
			conversationPane.append("> ");
			peer.bb().exec(receiveMsg.class, null, c -> true, false, text);
			textInput.setText("");
		});
	}

	class receiveMsg extends TypedInnerClassOperation {

		public void f(String msg) {
			conversationPane.append(msg + '\n');
		}

		@Override
		public String getDescription() {
			return "receives a piece of text";
		}

	}

	@Override
	public JComponent getComponent() {
		return panel;
	}

	@Override
	public String getFriendlyName() {
		return "chat";
	}
}