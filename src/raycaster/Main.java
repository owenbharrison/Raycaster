package raycaster;

import processing.core.*;
import java.util.ArrayList;

public class Main extends PApplet{
	public PVector pos;
	public PVector vel;
	public float rot = 0;
	public float speed = 3;
	public float radius = 8;
	
	public float rs = PI/64;
	public float fov = PI/1.6f;
	public int increment = 150;
	public int viewDist = 200;
	
  public boolean wKey = false;
  public boolean sKey = false;
  public boolean aKey = false;
  public boolean dKey = false;
  public ArrayList<PVector> path;
  
  public static void main(String[] args) {
  	PApplet.main(new String[] {"raycaster.Main"});
  }
  
  public void settings() {
  	size(600, 800);
  }
  
  public void keyPressed() {
  	if(key=='w')wKey=true;
  	if(key=='s')sKey=true;
  	if(key=='a')aKey=true;
  	if(key=='d')dKey=true;
  }
  
  public void keyReleased() {
  	if(key=='w')wKey=false;
  	if(key=='s')sKey=false;
  	if(key=='a')aKey=false;
  	if(key=='d')dKey=false;
  }
  
  public void mousePressed() {
  	path.add(new PVector(mouseX, mouseY));
  }
  
  public void setup() {
  	path = new ArrayList<PVector>();
  	pos = new PVector(0, 0);
  	vel = new PVector(0, 0);
  }
  
  public void draw() {
  	background(170);
  	if(wKey) {
  		vel = PVector.fromAngle(rot).mult(speed);
  	}
  	if(sKey) {
  		vel = PVector.fromAngle(rot).mult(speed*-0.75f);
  	}
  	pos.add(vel);
  	if(aKey)rot-=rs;
  	if(dKey)rot+=rs;
  	push();
    fill(255, 70);
    noStroke();
    beginShape();
    vertex(pos.x, pos.y);
  	float[] heights = new float[increment];
  	for(int n=0;n<increment;n++) {
  		float angle = map(n, 0, increment, rot-fov/2, rot+fov/2);
  		float record = viewDist;
  	  for(int i=0;i<path.size()-1;i++) {
  		  PVector curr = path.get(i);
  		  PVector next = path.get(i+1);
  		  PVector checkPos = PVector.fromAngle(angle).mult(viewDist).add(pos);
  		  PVector intersectPt = intersection(curr, next, pos, checkPos);
  		  if(intersectPt!=null) {
  		  	float dt = dist(pos.x, pos.y, intersectPt.x, intersectPt.y);
  		  	if(dt<record)record = dt;
  		  }
  	  }
  	  if(record==viewDist) {
  	  	heights[n] = record;
  	  }
  	  else{
  	  	heights[n] = record*cos(angle-rot);
  	  }
  	  PVector pt = PVector.fromAngle(angle).mult(record).add(pos);
  	  vertex(pt.x, pt.y);
  	}
  	endShape(CLOSE);
  	pop();
  	
  	//draw lines
  	push();
  	noFill();
    stroke(0);
    strokeWeight(2);
    beginShape();
    for(int i=0;i<path.size();i++) {
    	vertex(path.get(i).x, path.get(i).y);
    }
    endShape();
    pop();
    
    //draw player
    push();
    noStroke();
    fill(0, 255, 255);
    circle(pos.x, pos.y, radius*2);
    pop();
    
    //draw render background
    push();
    fill(0);
    noStroke();
    rect(0, 600, width, 200);
    pop();
    
    //render rays as lines at bottom
    for(int i=0;i<heights.length;i++) {
    	float x = map(i, 0, heights.length, 0, width);
    	float y = map(heights[i], viewDist, 0, 0, 100);
    	push();
    	stroke(map(heights[i], viewDist, 0, 0, 255));
    	strokeWeight(width/heights.length+1);
    	line(x, 700-y, x, 700+y);
    	pop();
    }
    
    //collisionLogic
    for(int i=1;i<path.size();i++) {
		  PVector curr = path.get(i);
		  PVector next = path.get(i-1);
		  PVector sub = PVector.sub(curr, next);
		  float angle = sub.heading();
		  PVector a = PVector.fromAngle(angle+PI/2).mult(radius).add(pos);
		  PVector b = PVector.fromAngle(angle-PI/2).mult(radius).add(pos);
		  PVector aIntersect = intersection(curr, next, a, pos);
		  while(aIntersect!=null) {
		  	pos.sub(PVector.sub(aIntersect, pos).normalize());
		  	a = PVector.fromAngle(angle+PI/2).mult(radius).add(pos);
			  b = PVector.fromAngle(angle-PI/2).mult(radius).add(pos);
			  aIntersect = intersection(curr, next, a, pos);
		  }
		  PVector bIntersect = intersection(curr, next, b, pos);
		  while(bIntersect!=null) {
		  	pos.sub(PVector.sub(bIntersect, pos).normalize());
		  	a = PVector.fromAngle(angle+PI/2).mult(radius).add(pos);
			  b = PVector.fromAngle(angle-PI/2).mult(radius).add(pos);
			  bIntersect = intersection(curr, next, b, pos);
		  }
    }
  	
  	surface.setTitle("Raycaster ... "+round(frameRate)+"fps");
  	vel = new PVector(0, 0);
  }
  
  public PVector intersection(PVector a, PVector b, PVector c, PVector d){
    float den = (a.x-b.x)*(c.y-d.y)-(a.y-b.y)*(c.x-d.x);
    float t = ((a.x-c.x)*(c.y-d.y)-(a.y-c.y)*(c.x-d.x))/den;
    float u = ((b.x-a.x)*(a.y-c.y)-(b.y-a.y)*(a.x-c.x))/den;
    if(t>=0.0&&t<=1.0&u>=0.0&&u<=1.0){
      return new PVector(a.x+t*(b.x-a.x), a.y+t*(b.y-a.y));
    }
    else return null;
  }
}
