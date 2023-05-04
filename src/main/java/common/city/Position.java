package common.city;

public class Position {
  public Integer x;
  public Integer y;

  public Position() {}
  public Position(Integer x,Integer y){
    this.x = x;
    this.y = y;
  }

  @Override
  public String toString() {
    return String.format("(%d,%d)",x,y);
  }
}
