/**
 * 
 */
package easyeda.sample.thing;

import java.util.List;

import ken.event.channel.BaseEventChannel;
import ken.event.processor.BaseEvtProc;

import backtype.storm.generated.StormTopology;

/**
 * @author KennyZJ
 *
 */
public class SampleThing extends StormTopology{

	private List<BaseEventChannel> channels;
	private List<BaseEvtProc> processors;
	
	
	public SampleThing(){
		super();
	}

}
