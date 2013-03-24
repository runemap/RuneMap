package com.thadb.rsmap;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.Timer;

public class MoveableImage extends JPanel{
	private static final long serialVersionUID = 1L;
	double x, y, startX, startY;
	 double scale = 1.0d;
     BufferedImage image;
     MouseHandler mouseHandler = new MouseHandler();
	 int baseWidth;
	 int baseHeight;
	 AffineTransform transform = new AffineTransform();
	private Location target;
	private Location start;
	
     MoveableImage() {
             setBackground(Color.white);
             setSize(450, 400);
             addMouseMotionListener(mouseHandler);
             addMouseListener(mouseHandler);
             addMouseWheelListener(mouseHandler);
             Image img = getToolkit().getImage(Main.class.getResource("/com/thadb/rsmap/rsmap.jpg"));
             MediaTracker mt = new MediaTracker(this);
             mt.addImage(img, 1);
             try {
                     mt.waitForAll();
             } catch (Exception e) {
                     System.out.println("Image not found.");
             }
             try{
             image = new BufferedImage(img.getWidth(this), img.getHeight(this),
                             BufferedImage.TYPE_INT_ARGB);
             baseWidth = image.getWidth();
             baseHeight = image.getHeight();
             Graphics2D g2 = image.createGraphics();
             g2.drawImage(img, 0, 0, this);
             
            
             setCenter(2010, 1740);
             
             
             Timer timer = new Timer(5, new ActionListener(){
            	 
            	 long lastTick = System.currentTimeMillis();
				@Override
				public void actionPerformed(ActionEvent e) {
					long thisTick = System.currentTimeMillis();
					long timeSinceLastTick = thisTick - lastTick;
					lastTick = thisTick;
					float delta = timeSinceLastTick;
					moveCenterTowardsTarget(delta);
				}
            	 
             });
             timer.start();
             
             } catch(Exception e){
            	 
             }

     }


	public void paintComponent(Graphics g) {
             super.paintComponent(g);
             Graphics2D g2D = (Graphics2D) g;
             g2D.transform(transform);
             g2D.drawImage(image, 0, 0, this);
     }

     public Location getMapFromScreen(Location loc){
    	 double x = (loc.x-transform.getTranslateX())/transform.getScaleX();
    	 double y = (loc.y-transform.getTranslateY())/transform.getScaleY();
    	 return new Location(x,y);
     }
     
     public Location getScreenFromMap(Location loc){
    	 double x = (loc.x+transform.getTranslateX())*transform.getScaleX();
    	 double y = (loc.y+transform.getTranslateY())*transform.getScaleY();
    	 return new Location(x,y);
     }
     
     public int getPixelWidth(){
    	 return (int) (image.getWidth()*transform.getScaleX());
     }
     
     public int getPixelHeight(){
    	 return (int) (image.getHeight()*transform.getScaleY());
     }
     
     public boolean checkBounds(){
    	 double currTranslateX = transform.getTranslateX();
    	 double currTranslateY = transform.getTranslateY();
    	 double minTranslateX = -(image.getWidth()*transform.getScaleX())+getWidth();
    	 double minTranslateY = -(image.getHeight()*transform.getScaleY())+getHeight();
    	 double maxTranslateX = 0;
    	 double maxTranslateY = 0;
    	 double newScale;
    	 boolean status = true;
    	 if (image.getWidth()*transform.getScaleX() < getWidth()){
    		 newScale = getWidth()/(image.getWidth()*transform.getScaleX());
    		 transform.scale(newScale, newScale);
    		 status = false;
    	 }
    	 if (image.getHeight()*transform.getScaleY() < getHeight()){
    		 newScale = getHeight()/(image.getHeight()*transform.getScaleY());
    		 transform.scale(newScale, newScale);
    		 status = false;
    	 }
    	 if (currTranslateX > maxTranslateX){
    		 transform.translate(-currTranslateX/transform.getScaleX(), 0);
    		 status = false;
    	 }
    	 if (currTranslateY > maxTranslateY){
    		 transform.translate(0, -currTranslateY/transform.getScaleY());
    		 status = false;
    	 }
    	 if (currTranslateX < minTranslateX){
    		 double dX = minTranslateX - currTranslateX;
    		transform.translate(dX/transform.getScaleX(), 0);
    		status = false;
    	 }
    	 if (currTranslateY < minTranslateY){
    		 double dY = minTranslateY - currTranslateY;
    		 transform.translate(0, dY/transform.getScaleY());
    		 status = false;
    	 }
    	 repaint();
    	 return status;
     }
     
     public Location getCenter(){
    	 Location centerLoc = new Location(getWidth()/2, getHeight()/2);
    	 return getMapFromScreen(centerLoc);
     }
     
     public void setCenter(Location mapLoc){
    	 Location centerMapLoc = getMapFromScreen(new Location(getWidth()/2, getHeight()/2));
    	 double dX = centerMapLoc.x - mapLoc.x;
    	 double dY = centerMapLoc.y - mapLoc.y;
    	 transform.translate(dX, dY);
    	 checkBounds();
     }
     
     public void setTarget(Location mapLoc){
    	 target = mapLoc;
    	 start = null;
     }

     public double getAngle(Location start, Location target) {
    	    double angle =  Math.toDegrees(Math.atan2(target.x - start.x, target.y - start.y));

    	    if(angle < 0){
    	        angle += 360;
    	    }

    	    return angle;
    	}
     
     public double angle = 0;
	public void moveCenterTowardsTarget(double delta) {
		if (target != null) {
			float movespeed = 0.6f;
			Location centerMapLoc = getMapFromScreen(new Location(getWidth() / 2, getHeight() / 2));
			if (start == null){
				start = centerMapLoc;
			}else{
			double angle = getAngle(start, target);
				angle = Math.toRadians(angle);
				double sv = (-Math.cos(angle)*movespeed*delta)/transform.getScaleX();
			    double sh = (-Math.sin(angle)*movespeed*delta)/transform.getScaleY();
			    double maxDist = Math.sqrt(Math.pow(sh,2)+Math.pow(sv, 2))*1.5d;
				if (distanceBetween(centerMapLoc, target) > maxDist){
					transform.translate(sh, sv);
					if(!checkBounds()){
						target = null;
						start = null;
					}
				}else{
					setCenter(target);
					target= null;
					start = null;
				}
			}

			


		}
     }
     
     private double distanceBetween(Location start, Location end) {
		return Math.sqrt(Math.pow(start.x-end.x,2) + Math.pow(start.y-end.y,2));
	}


	private void setCenter(int x, int y) {
		setCenter(new Location(x,y));
	}
     
     class MouseHandler implements MouseMotionListener, MouseListener, MouseWheelListener {
             public void mouseDragged(MouseEvent e) {
            	 target= null;
            	 start = null;
            	 x = (e.getX() - startX)/transform.getScaleX();
            	 y = (e.getY() - startY)/transform.getScaleY();
            	 startX = e.getX();
            	 startY = e.getY();
                 transform.translate(x, y);
                 checkBounds();
                 System.out.println("center: "+getCenter());
             }

             public void mouseMoved(MouseEvent e) {
            	 Location loc = new Location(e.getX(), e.getY());
            	 System.out.println(getMapFromScreen(loc));
             }

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && e.getButton() == 1){
				Location loc = new Location(e.getX(), e.getY());
				setTarget(getMapFromScreen(loc));
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {

				
			}

			@Override
			public void mouseExited(MouseEvent e) {
				
			}

			@Override
			public void mousePressed(MouseEvent e) {
				startX = e.getX();
				startY = e.getY();
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				
			}

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				double oldTotalScale = transform.getScaleX();
				scale = 1 + -e.getPreciseWheelRotation()*0.15d;
				double newTotalScale = oldTotalScale * scale;
				if ((image.getHeight()*newTotalScale < getHeight()) || (image.getWidth()*newTotalScale < getWidth())){
					//return;
				}else{
					Location screenMouse = new Location(e.getX(), e.getY());
					Location prevMapMouse = getMapFromScreen(screenMouse);
					transform.scale(scale,scale);
					Location newMapMouse = getMapFromScreen(screenMouse);
					double dX = newMapMouse.x - prevMapMouse.x;
					double dY = newMapMouse.y - prevMapMouse.y;
					transform.translate(dX, dY);				
				}
				checkBounds();
			}
     }
     
     public class Location {
    	 @Override
		public String toString() {
			return "x: "+x+" y: "+y;
		}
		double x, y;
    	Location(double x, double y){
    		this.x = x;
    		this.y = y;
    	}
     }
}
