package cubix.transitions.animation;

/**
 * Implemented by objects that want to be informed about the 
 * ending of an animation.
 * 
 * @author benjamin.bach@inria.fr
 *
 */
public interface AnimationListener {
	
	public void animationFinishedHandler(Animation a);

}
