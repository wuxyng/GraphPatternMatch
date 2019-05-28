package helper;

public class Util {

	
	 public static int getUnsignedByte (byte data){      //将data字节型数据转换为0~255 (0xFF 即BYTE)。
         return data&0x0FF ;
      }

      public static int getUnsignedByte (short data){      //将data字节型数据转换为0~65535 (0xFFFF 即 WORD)。
            return data&0x0FFFF ;
      }       

     public static long getUnsignedInt (int data){     //将int数据转换为0~4294967295 (0xFFFFFFFF即DWORD)。
         return data&0x0FFFFFFFF ;
      }
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
