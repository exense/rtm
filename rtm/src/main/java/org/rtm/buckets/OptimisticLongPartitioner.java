package org.rtm.buckets;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OptimisticLongPartitioner extends RangePartitioner<Long>{
	
	private static final Logger logger = LoggerFactory.getLogger(OptimisticLongPartitioner.class);

	//private LongAccumulator la;
	private AtomicLong al;

	public OptimisticLongPartitioner(Long min, Long max, Long incrementSize) {
		super(min, max, incrementSize);
		//la = new LongAccumulator((x, y) -> x + y, min);
		al = new AtomicLong(min);
	}

	/*
	 * @return returns a RangeBucket if there's still an available range in the partition, or null if the partition has been exhausted.
	 *  
	 * @see org.rtm.buckets.RangePartitioner#hasNext()
	 * @see org.rtm.buckets.RangePartitioner#nextIfHas()
	 */
	@Override
	public RangeBucket<Long> next() throws IllegalStateException {
		Long newLower = al.longValue();

		/*
		 * 
		 * We deliberately tolerate this illegal state as a colateral of the optimistic mechanism
		 * because hasNext() can not provide a strong garantee that there's still something to do.
		 * 
		 * The caller needs to test null case explicitely.
		 * 
		 */
		if(newLower > super.max){
			//throw new IllegalStateException("Partition exhausted: newLower="+newLower+"; max="+super.max);
			//logger.warn("Partition exhausted: newLower="+newLower+"; max="+super.max);
			
			return null;
		}
		//la.accumulate(super.incrementSize);
		Long targetUpper = newLower + super.incrementSize;
		if(al.compareAndSet(newLower, targetUpper))
		{
			Long newVal = targetUpper;
			Long newUpper;
			if(newVal >= super.max)
				newUpper = super.max;
			else
				newUpper = newVal;
			
			
			return new RangeBucket<Long>(newLower, newUpper);
		}else{
			return next();
		}
	}

	
	/*
	 * @return optimistically returns true if there was an available range in the partition at the time of checking, or false if the partitioner has been exhausted 
	 * 
	 * @see org.rtm.buckets.RangePartitioner#getNextBucket()
	 * @see org.rtm.buckets.RangePartitioner#nextIfHas()
	 */
	@Override
	public boolean hasNext() {
		return al.longValue() < max;
	}
	
}
