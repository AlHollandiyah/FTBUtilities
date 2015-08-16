package latmod.ftbu.core;

import java.io.*;

import latmod.ftbu.core.util.*;
import net.minecraft.nbt.*;
import net.minecraftforge.common.util.Constants;

@SuppressWarnings("all")
public class LMNBTUtils
{
	public static final byte END = Constants.NBT.TAG_END;
	public static final byte BYTE = Constants.NBT.TAG_BYTE;
	public static final byte SHORT = Constants.NBT.TAG_SHORT;
	public static final byte INT = Constants.NBT.TAG_INT;
	public static final byte LONG = Constants.NBT.TAG_LONG;
	public static final byte FLOAT = Constants.NBT.TAG_FLOAT;
	public static final byte DOUBLE = Constants.NBT.TAG_DOUBLE;
	public static final byte BYTE_ARRAY = Constants.NBT.TAG_BYTE_ARRAY;
	public static final byte STRING = Constants.NBT.TAG_STRING;
	public static final byte LIST = Constants.NBT.TAG_LIST;
	public static final byte MAP = Constants.NBT.TAG_COMPOUND;
	public static final byte INT_ARRAY = Constants.NBT.TAG_INT_ARRAY;
	
	public static String[] getMapKeysA(NBTTagCompound tag)
	{
		if(tag == null || tag.hasNoTags()) return new String[0];
		return (String[]) tag.func_150296_c().toArray(new String[0]);
	}
	
	public static FastList<String> getMapKeys(NBTTagCompound tag)
	{
		FastList<String> list = new FastList<String>();
		if(tag == null || tag.hasNoTags()) return list;
		list.addAll(tag.func_150296_c()); return list;
	}
	
	public static FastMap<String, NBTBase> toFastMap(NBTTagCompound tag)
	{
		FastMap<String, NBTBase> map = new FastMap<String, NBTBase>();
		FastList<String> keys = getMapKeys(tag);
		for(int i = 0; i < keys.size(); i++)
		{ String s = keys.get(i); map.put(s, tag.getTag(s)); }
		return map;
	}
	
	public static <E extends NBTBase> FastMap<String, E> toFastMapWithType(NBTTagCompound tag)
	{
		FastMap<String, E> map = new FastMap<String, E>();
		FastList<String> keys = getMapKeys(tag);
		
		for(int i = 0; i < keys.size(); i++)
		{
			String s = keys.get(i);
			map.put(s, (E)tag.getTag(s));
		}
		
		return map;
	}
	
	public static void writeMap(OutputStream os, NBTTagCompound tag) throws Exception
	{
		byte[] b = CompressedStreamTools.compress(tag);
		os.write(b); os.flush(); os.close();
	}
	
	public static Exception writeMap(File f, NBTTagCompound tag)
	{
		try { writeMap(new FileOutputStream(LMFileUtils.newFile(f)), tag); }
		catch(Exception e) { return e; } return null;
	}
	
	public static NBTTagCompound readMap(InputStream is) throws Exception
	{
		byte[] b = new byte[is.available()]; is.read(b); is.close();
		return CompressedStreamTools.func_152457_a(b, NBTSizeTracker.field_152451_a);
	}
	
	public static NBTTagCompound readMap(File f)
	{
		if(f == null || !f.exists()) return null;
		try { return readMap(new FileInputStream(f)); }
		catch(Exception e) { e.printStackTrace(); }
		return null;
	}
	
	public static boolean areTagsEqual(NBTTagCompound tag1, NBTTagCompound tag2)
	{
		if(tag1 == null && tag2 == null) return true;
		if(tag1 != null && tag2 == null) return false;
		if(tag1 == null && tag2 != null) return false;
		return tag1.equals(tag2);
	}
	
	public static void toStringList(FastList<String> l, NBTTagList tag)
	{
		l.clear();
		for(int i = 0; i < tag.tagCount(); i++)
			l.add(tag.getStringTagAt(i));
	}
	
	public static FastList<String> toStringList(NBTTagList tag)
	{ FastList<String> l = new FastList<String>(); toStringList(l, tag); return l; }
	
	public static NBTTagList fromStringList(FastList<String> l)
	{
		NBTTagList tag = new NBTTagList();
		for(int i = 0; i < l.size(); i++)
			tag.appendTag(new NBTTagString(l.get(i)));
		return tag;
	}
}