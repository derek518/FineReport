package com.fr.design.style;

import com.fr.design.layout.FRGUIPaneFactory;
import com.fr.design.style.background.BackgroundJComponent;
import com.fr.design.style.background.gradient.GradientPane;
import com.fr.general.Background;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kunsnat E-mail:kunsnat@gmail.com
 * @version ����ʱ�䣺2011-11-24 ����02:51:12
 *          ��˵��: ����box
 */
public abstract class AbstractPopBox extends JPanel {

	protected BackgroundJComponent displayComponent;

	private JWindow selectPopupWindow;
	private boolean isWindowEventInit = false;
    private static int GAP = 2;
    private static int GAP2 = 20;

	private List<ChangeListener> changeListenerList = new ArrayList<ChangeListener>();

	MouseAdapter mouseListener = new MouseAdapter() {
		public void mousePressed(MouseEvent evt) {
			showPopupMenu();
		}

		public void mouseExited(MouseEvent evt) {
			int x= evt.getXOnScreen();//mac��widows�ļ���������һ��
        	int y = evt.getYOnScreen();
			if (selectPopupWindow != null) {
                Rectangle rectangle = selectPopupWindow.getBounds();
                boolean b1 = x < rectangle.x - GAP || x > rectangle.x + rectangle.width + GAP;
                boolean b2 = y < rectangle.y - GAP || y >rectangle.y + rectangle.height + GAP;
                if(b1 || b2) {
                    hidePopupMenu();
                }
//				if (OperatingSystem.isWindows()) {
//
//				}else{
//					Point pp = SwingUtilities.convertPoint(evt.getComponent(), evt.getPoint(), selectPopupWindow.getParent());
//					Rectangle rectangle = selectPopupWindow.getBounds();
//					if (pp.getY() < rectangle.getY() || pp.getY() > rectangle.getY() + rectangle.getHeight()) {
//						//hidePopupMenu();
//					}
//				}
			}
		}
	};

	private void showPopupMenu() {
		if (selectPopupWindow != null && selectPopupWindow.isVisible()) {
			hidePopupMenu();
			return;
		}

		if (!this.isEnabled()) {
			return;
		}

		selectPopupWindow = this.getControlWindow();

		Point convertPoint = new Point(0, 0);

		// e: ����(0,0)��ColorSelectionPane������ϵͳת������Ļ����.
		SwingUtilities.convertPointToScreen(convertPoint, this);
		int y = convertPoint.y + this.getSize().height;
		int x = convertPoint.x;
		int h = y + selectPopupWindow.getHeight();
		int width = x + selectPopupWindow.getWidth();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		if (h > screenSize.height) {
			y = y - selectPopupWindow.getHeight() - GAP2;// ������Ļ�߶���
		}
		
		if(width > screenSize.width) {
			x = screenSize.width - selectPopupWindow.getWidth();
		}
		selectPopupWindow.setLocation(x, y);

		selectPopupWindow.setVisible(true);

		//wei : Ϊ�˵����ĵط�������ɫ������ʧ
		MouseAdapter parentMouseListener = new MouseAdapter() {
			public void mousePressed(MouseEvent evt) {
				mouseClick(evt);
			}
		};
		if (!this.isWindowEventInit && SwingUtilities.getAncestorOfClass(GradientPane.class, this) != null) {
			SwingUtilities.getAncestorOfClass(GradientPane.class, this).addMouseListener(parentMouseListener);
			this.isWindowEventInit = true;
		}
	}

	private void mouseClick(MouseEvent evt) {
		int x = evt.getLocationOnScreen().x;
		int y = evt.getLocationOnScreen().y;
		Rectangle rectangle = this.getControlWindow().getBounds();
        boolean b1 = x < rectangle.x || x > rectangle.x + rectangle.width;
        boolean b2 = y < rectangle.y || y > rectangle.y + rectangle.height;
		if (b1 || b2) {
			this.hidePopupMenu();
		}
	}

	protected void hidePopupMenu() {
		if (selectPopupWindow != null) {
			selectPopupWindow.setVisible(false);
		}

		selectPopupWindow = null;
	}

	protected JWindow getControlWindow() {
		//find parent.
		if (this.selectPopupWindow == null) {
			Window parentWindow = SwingUtilities.windowForComponent(this);
			if (parentWindow != null) {
				this.selectPopupWindow = new SelectControlWindow(parentWindow);
			}

			selectPopupWindow.addMouseListener(new MouseAdapter() {
				public void mouseExited(MouseEvent evt) {
					int x = evt.getLocationOnScreen().x;
					int y = evt.getLocationOnScreen().y;

					if (selectPopupWindow != null) {
						double desValue = 2;
						Rectangle rectangle = selectPopupWindow.getBounds();
                        boolean b1 = x < rectangle.x + desValue || x >= rectangle.x + rectangle.width - desValue;
                        boolean b2 = y < rectangle.y + desValue || y > rectangle.y + rectangle.height - desValue;
                        if (b1 || b2) {
							hidePopupMenu();
						}
					}
				}
			});
		}

		return selectPopupWindow;
	}

    /**
     * �����¼�
     * @param changeListener �¼�
     */
	public void addSelectChangeListener(ChangeListener changeListener) {
		this.changeListenerList.add(changeListener);
	}

    /**
     * ɾ���¼�
     * @param changeListener �¼�
     */
	public void removeSelectChangeListener(ChangeListener changeListener) {
		this.changeListenerList.remove(changeListener);
	}

    /**
     * ��Ӧ�¼�
     */
	public void fireChangeListener() {
		if (!changeListenerList.isEmpty()) {
			ChangeEvent evt = new ChangeEvent(this);
			for (int i = 0; i < changeListenerList.size(); i++) {
				this.changeListenerList.get(i).stateChanged(evt);
			}
		}
	}

    /**
     * ��˵��
     * @param background ����
     */
	public void fireDisplayComponent(Background background) {
		if (displayComponent != null) {
			displayComponent.setSelfBackground(background);
		}
		fireChangeListener();
		this.repaint();
	}

    /**
     * ��ʼ������������
     * @param preWidth ����
     * @return �������
     */
	public abstract JPanel initWindowPane(double preWidth);

	private class SelectControlWindow extends JWindow {
		private static final long serialVersionUID = -5776589767069105911L;

		public SelectControlWindow(Window paranet) {
			super(paranet);
			this.initComponents();
		}

		public void initComponents() {
			JPanel defaultPane = FRGUIPaneFactory.createBorderLayout_S_Pane();
			this.setContentPane(defaultPane);

//            defaultPane.setBorder(UIManager.getBorder("PopupMenu.border"));

			if (displayComponent != null) {
				defaultPane.add(initWindowPane(displayComponent.getPreferredSize().getWidth()));
			} else {
				defaultPane.add(initWindowPane(20));
			}
			this.pack();
		}

		@Override
		public void setVisible(boolean b) {
			super.setVisible(b);
			AbstractPopBox.this.repaint();
		}
	}

	protected boolean isPopupVisible() {
		return selectPopupWindow == null ? false : selectPopupWindow.isVisible();
	}
}