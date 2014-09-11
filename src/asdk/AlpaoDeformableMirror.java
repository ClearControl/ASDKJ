package asdk;

import org.bridj.Pointer;

import asdk.bindings.ASDKLibrary;
import asdk.bindings.ASDKLibrary.DM;

public class AlpaoDeformableMirror
{

	private String mAlpaoDeviceSerialName;
	private boolean mDebugPrintout = false;
	private Pointer<DM> mDevicePointer;

	public AlpaoDeformableMirror(String pAlpaoDeviceSerialName)
	{
		super();
		mAlpaoDeviceSerialName = pAlpaoDeviceSerialName;
	}

	public boolean initialize()
	{
		if (mDevicePointer != null)
			return false;
		Pointer<Byte> lPointerToSerialNumber = Pointer.pointerToCString(mAlpaoDeviceSerialName);
		if (isDebugPrintout())
			System.out.println("ASDKLibrary.asdkInit(...");
		mDevicePointer = ASDKLibrary.asdkInit(lPointerToSerialNumber);
		lPointerToSerialNumber.release();
		if (isDebugPrintout())
			printLastError();

		String lLastErrorString = getLastErrorString();
		if (isError(lLastErrorString))
			return false;

		return true;
	}

	public int getNumberOfActuators()
	{
		Pointer<Double> lPointerToNumberOfActuators = Pointer.allocateDouble();
		if (isDebugPrintout())
			System.out.println("ASDKLibrary.asdkGet(...NbOfActuator...");
		ASDKLibrary.asdkGet(mDevicePointer,
												Pointer.pointerToCString("NbOfActuator"),
												lPointerToNumberOfActuators);
		if (isDebugPrintout())
			printLastError();
		int lNumberOfActuators = (int) lPointerToNumberOfActuators.getDouble();
		lPointerToNumberOfActuators.release();
		if (isDebugPrintout())
			System.out.println("lNumberOfActuators=" + lNumberOfActuators);

		return lNumberOfActuators;
	}

	public boolean setInputTriggerMode(TriggerMode pTriggerMode)
	{
		if (isDebugPrintout())
			System.out.println("ASDKLibrary.asdkSet(...TriggerIn..." + pTriggerMode);
		ASDKLibrary.asdkSet(mDevicePointer,
												Pointer.pointerToCString("TriggerIn"),
												pTriggerMode.ordinal());
		if (isDebugPrintout())
			printLastError();

		String lLastErrorString = getLastErrorString();
		if (isError(lLastErrorString))
			return false;

		return true;
	}

	public boolean sendOneMirorShape(double[] pMirrorShape)
	{
		if (isDebugPrintout())
			System.out.println("ASDKLibrary.asdkSend(...");
		Pointer<Double> lPointerToDoubleArray = Pointer.pointerToDoubles(pMirrorShape);
		ASDKLibrary.asdkSend(mDevicePointer, lPointerToDoubleArray);
		if (isDebugPrintout())
			printLastError();

		String lLastErrorString = getLastErrorString();
		if (isError(lLastErrorString))
			return false;

		return true;
	}

	public boolean sendMultipleMirrorShapesAsynchronously(double[] pMirrorShape,
																												final int pNumberOfPatterns,
																												final int pNumberOfRepeats)
	{
		if (isDebugPrintout())
			System.out.println("ASDKLibrary.asdkSend(...");
		Pointer<Double> lPointerToDoubleArray = Pointer.pointerToDoubles(pMirrorShape);
		ASDKLibrary.asdkSendPattern(mDevicePointer,
																lPointerToDoubleArray,
																pNumberOfPatterns,
																pNumberOfRepeats);
		if (isDebugPrintout())
			printLastError();

		String lLastErrorString = getLastErrorString();
		if (isError(lLastErrorString))
			return false;

		return true;
	}

	public boolean release()
	{
		if (isDebugPrintout())
			System.out.println("ASDKLibrary.asdkRelease(...");
		ASDKLibrary.asdkRelease(mDevicePointer);
		if (isDebugPrintout())
			printLastError();

		String lLastErrorString = getLastErrorString();
		if (isError(lLastErrorString))
			return false;

		return true;
	}

	private boolean isError(String pErrorString)
	{
		return !pErrorString.toLowerCase().contains("no error");
	}

	public String getLastErrorString()
	{
		Pointer<Integer> errorNo = Pointer.allocateInt();
		Pointer<Byte> errMsg = Pointer.allocateBytes(256);
		long errSize = 256;
		ASDKLibrary.asdkGetLastError(errorNo, errMsg, errSize);
		String lErrorString = new String(errMsg.getBytes());
		errorNo.release();
		errMsg.release();
		return lErrorString;
	}

	public void printLastError()
	{
		ASDKLibrary.asdkPrintLastError();
	}

	public boolean isDebugPrintout()
	{
		return mDebugPrintout;
	}

	public void setDebugPrintout(boolean pDebugPrintout)
	{
		mDebugPrintout = pDebugPrintout;
	}

}
