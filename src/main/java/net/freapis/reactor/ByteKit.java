package net.freapis.reactor;

/**
 * 
 * @author wuqiang
 * 字节工具类，提供字节的基本转换
 */
public class ByteKit {
	
	private final static char[] digits = {
			'0' , '1' , '2' , '3' , '4' , '5' ,
	        '6' , '7' , '8' , '9' , 'a' , 'b' ,
	        'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
	        'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
	        'o' , 'p' , 'q' , 'r' , 's' , 't' ,
	        'u' , 'v' , 'w' , 'x' , 'y' , 'z'
	};
	
	public static String toBinaryString(byte b) {
		char[] buf = new char[8];
		int charPos = 8;
		do{
			buf[--charPos] = digits[b & 0x01];
			b >>>= 1;
		}while(charPos > 0);
		return new String(buf);
	}

	public static String toBinaryString(byte[] bytes,boolean pretty){
		StringBuilder binaryString = new StringBuilder();
		int prettyPoint = 0;
		for(byte b : bytes){
			binaryString.append(toBinaryString(b));
			if(pretty){
				binaryString.append(0x20);
				prettyPoint++;
				if(prettyPoint % 8 == 0) binaryString.append("\n");
			}
		}
		return binaryString.toString();
	}
	
	public static String toHexString(byte b){
		return new String(new char[]{
				digits[b >>> 4 & 0x0f],
				digits[b & 0x0f]
		});
	}

	public static String toHexString(byte[] bytes,boolean pretty){
		StringBuilder hexString = new StringBuilder();
		int prettyPoint = 0;
		for(byte b : bytes){
			hexString.append(toHexString(b));
			if(pretty){
				hexString.append(" ");
				prettyPoint++;
				if(prettyPoint % 16 == 0) hexString.append("\n");
			}
		}
		return hexString.toString();
	}

	public static int toInt(byte[] source) {
		if (source == null || source.length == 0)
			return 0;
		if (source.length > 4)
			throw new IllegalArgumentException("int value must be less than 32 bits");
		int result = 0;
		for (int i = source.length - 1, shift = 0; i >= 0; i--, shift++) {
			result |= ((source[i] & 0xff) << (shift * 8));
		}
		return result;
	}

	public static long toLong(byte[] source) {
		if (source == null || source.length == 0)
			return 0;
		if (source.length > 8)
			throw new IllegalArgumentException("long value must be less than 64 bits");
		long result = 0;
		for (int i = source.length - 1, shift = 0; i >= 0; i--, shift++) {
			result |= (long) (source[i] & 0xff) << (shift * 8);
		}
		return result;
	}

	public static byte[] longToBytes(long num) {
		byte[] b = new byte[8];
		for (int i = 0; i < 8; i++) {
			b[i] = (byte) (num >>> (56 - (i * 8)));
		}
		return b;
	}

	public static byte[] intToBytes(int num) {
		byte[] b = new byte[4];
		for (int i = 0; i < 4; i++) {
			b[i] = (byte) (num >>> (24 - (i * 8)));
		}
		return b;
	}

	/**
	 * reverse the byte array
	 * @param source byte array to reverse
	 * @param groupLength
	 * peer group length,for example,
	 * if you want reverse byte array according it's int values,
	 * then the group length is 4,if according it's long values,
	 * then the group length is 8.
     */
	public static void reverseBytes(byte[] source,int groupLength){

		int totalLength = source.length;
		if(totalLength % groupLength != 0 || groupLength == 0)
			throw new IllegalArgumentException("invalid stepLength.");
		int groupCount =  totalLength / groupLength;

		byte swap;
		int highStepPos;
		int lowStepPos;

		for(int i = 0; i < groupCount / 2; i++){
			for(int j = 0; j < groupLength; j++){
				lowStepPos = i * groupLength + j;
				highStepPos = (groupCount - i - 1) * groupLength + j;
				swap = source[lowStepPos];
				source[lowStepPos] = source[highStepPos];
				source[highStepPos] = swap;
			}
		}
	}

	public static byte[][] split2Groups(byte[] source,int groupLength){
		int totalLength = source.length;
		if(totalLength % groupLength != 0 || groupLength == 0)
			throw new IllegalArgumentException("invalid stepLength.");
		int groupCount =  totalLength / groupLength;

		byte[][] result = new byte[groupCount][groupLength];
		for(int i = 0; i < groupCount; i++){
			for(int j = 0; j < groupLength; j++){
				result[i][j] = source[i * groupLength + j];
			}
		}
		return result;
	}
	
	public static void main(String[] args) {
		/*System.out.println(toString(new byte[]{-128,127,32,64},16));
		System.out.println(toInt(new byte[]{-1,-1,-1}));
		System.out.println(toLong(new byte[]{127,-1,-1,-1,-1,-1,-1,-1}));
		
		System.out.println(toLong(longToBytes(Long.MAX_VALUE)));
		System.out.println(toInt(intToBytes(Integer.MAX_VALUE)));
		
		System.out.println(toString(intToBytes(2), 2));*/
		//System.out.println(toString(intToBytes(-1),2));
		System.out.println(toHexString(longToBytes(216172782113783808L),false));
	}
}
