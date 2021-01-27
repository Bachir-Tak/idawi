package idawi.demo;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import idawi.Component;
import idawi.Service;
import idawi.To;
import toools.gui.Swingable;

public class Chat extends Service implements Swingable {
	private final JPanel panel = new JPanel(new BorderLayout());

	public Chat(Component peer) {
		super(peer);
		JTextArea conversationPane = new JTextArea();
		JTextField textInput = new JTextField();
		panel.add(conversationPane, BorderLayout.CENTER);
		panel.add(textInput, BorderLayout.SOUTH);
		textInput.requestFocus();

		textInput.addActionListener(e -> {
			String text = textInput.getText().trim();
			send(text, new To(), null);
			textInput.setText("");
		});

		registerOperation(null, (in, out) -> {
			var msg = in.get_non_blocking();

			if (msg.route.source().equals(peer.descriptor())) {
				conversationPane.append("> ");
			}

			conversationPane.append(msg.content.toString() + '\n');
			send(msg.content, new To(), null);
		});
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
