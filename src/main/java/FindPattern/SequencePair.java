package FindPattern;

import java.util.ArrayList;


public class SequencePair<T1,T2> {
		
		public ArrayList<T1> firstSeq;
		public ArrayList<T2> secondSeq;
		//public int size;
		
		public SequencePair(ArrayList<T1> firstSeq, ArrayList<T2> secondSeq) {
			this.firstSeq = firstSeq;
			this.secondSeq = secondSeq;
		}
	
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((firstSeq == null) ? 0 : firstSeq.hashCode());
			result = prime * result
					+ ((secondSeq == null) ? 0 : secondSeq.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SequencePair other = (SequencePair) obj;
			if (firstSeq == null) {
				if (other.firstSeq != null)
					return false;
			} else if (!firstSeq.equals(other.firstSeq))
				return false;
			if (secondSeq == null) {
				if (other.secondSeq != null)
					return false;
			} else if (!secondSeq.equals(other.secondSeq))
				return false;
			return true;
		}

		
	}
	