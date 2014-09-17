package asdk;

import java.io.Closeable;
import java.io.IOException;

import org.bridj.Pointer;

import asdk.bindings.ASDKLibrary;
import asdk.bindings.ASDKLibrary.DM;

public class AlpaoDeformableMirror implements Closeable
{
	private Object mLock = new Object();

	private String mAlpaoDeviceSerialName;
	private Pointer<DM> mDevicePointer;
	private boolean mDebugPrintout = false;

	private Pointer<Double> mRawMirrorShapeVector;
	private Pointer<Double> mRawMirrorShapeSequenceVector;

	public AlpaoDeformableMirror(String pAlpaoDeviceSerialName)
	{
		super();
		mAlpaoDeviceSerialName = pAlpaoDeviceSerialName;
	}

	public boolean open()
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

		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					close();
				}
				catch (Throwable e)
				{
					e.printStackTrace();
				}
			}
		});

		return true;
	}

	public void close() throws IOException
	{
		synchronized (mLock)
		{
			if (mDevicePointer == null)
				return;

			if (isDebugPrintout())
				System.out.println("ASDKLibrary.asdkRelease(...");
			ASDKLibrary.asdkRelease(mDevicePointer);
			mDevicePointer = null;
			if (isDebugPrintout())
				printLastError();

			String lLastErrorString = getLastErrorString();
			if (isError(lLastErrorString))
				throw new AlpaoException("ALPAO:" + lLastErrorString);

		}
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

	public boolean sendFlatMirrorShapeVector()
	{
		return mDebugPrintout;

	}

	public boolean sendFullMatrixMirrorShapeVector(Pointer<Double> pFullMatrixMirrorShapeVectorDoubleBuffer)
	{
		checkVectorDimensions(pFullMatrixMirrorShapeVectorDoubleBuffer,
													AlpaoDeformableMirrorsSpecifications.getFullMatrixLength(getNumberOfActuators()));
		Pointer<Double> lRawMirrorShapeVectorDoubleBuffer = removeNonExistantCornerActuators(pFullMatrixMirrorShapeVectorDoubleBuffer);

		return sendRawMirrorShapeVector(lRawMirrorShapeVectorDoubleBuffer);

	}

	public boolean sendFullMatrixMirrorShapeSequenceVector(	Pointer<Double> pFullMatrixMirrorShapeVectorDoubleBuffer,
																													final int pNumberOfPatterns,
																													final int pNumberOfRepeats)
	{
		// TODO: add the same kind of function for the MIRAO, we need a way to do
		// the conversion on sequences too.
		// use: mRawMirrorShapeSequenceVector
		return mDebugPrintout;
	}

	private Pointer<Double> removeNonExistantCornerActuators(Pointer<Double> pSquareMirrorShapeVectorDoubleBuffer)
	{
		checkVectorDimensions(pSquareMirrorShapeVectorDoubleBuffer,
													AlpaoDeformableMirrorsSpecifications.getFullMatrixLength(getNumberOfActuators()));
		if (mRawMirrorShapeVector == null || mRawMirrorShapeVector.getValidElements() != getNumberOfActuators())
			mRawMirrorShapeVector = Pointer.allocateDoubles(getNumberOfActuators());

		AlpaoDeformableMirrorsSpecifications.convertLayout(	getNumberOfActuators(),
																												pSquareMirrorShapeVectorDoubleBuffer,
																												mRawMirrorShapeVector);
		return mRawMirrorShapeVector;
	}

	public boolean sendRawMirrorShapeVector(double[] pMirrorShape)
	{
		Pointer<Double> lPointerToDoubleArray = Pointer.pointerToDoubles(pMirrorShape);
		boolean lReturnValue = sendRawMirrorShapeVector(lPointerToDoubleArray);
		lPointerToDoubleArray.release();
		return lReturnValue;
	}

	public boolean sendRawMirrorShapeVector(Pointer<Double> pMirrorShape)
	{
		synchronized (mLock)
		{
			if (isDebugPrintout())
				System.out.println("ASDKLibrary.asdkSend(...");
			ASDKLibrary.asdkSend(mDevicePointer, pMirrorShape);
			if (isDebugPrintout())
				printLastError();

			String lLastErrorString = getLastErrorString();
			if (isError(lLastErrorString))
				return false;

			return true;
		}
	}

	public boolean sendMirrorShapeSequenceAsynchronously(	double[] pMirrorShape,
																												final int pNumberOfPatterns,
																												final int pNumberOfRepeats)
	{

		Pointer<Double> lPointerToDoubleArray = Pointer.pointerToDoubles(pMirrorShape);
		boolean lReturnValue = sendMirrorShapeSequenceAsynchronously(	lPointerToDoubleArray,
																																	pNumberOfPatterns,
																																	pNumberOfRepeats);
		lPointerToDoubleArray.release();
		return lReturnValue;
	}

	public boolean sendMirrorShapeSequenceAsynchronously(	Pointer<Double> pMirrorShape,
																												final int pNumberOfPatterns,
																												final int pNumberOfRepeats)
	{
		synchronized (mLock)
		{
			if (isDebugPrintout())
				System.out.println("ASDKLibrary.asdkSend(...");
			ASDKLibrary.asdkSendPattern(mDevicePointer,
																	pMirrorShape,
																	pNumberOfPatterns,
																	pNumberOfRepeats);
			if (isDebugPrintout())
				printLastError();

			String lLastErrorString = getLastErrorString();
			if (isError(lLastErrorString))
				return false;

			return true;
		}
	}

	private boolean isError(String pErrorString)
	{
		return !pErrorString.toLowerCase().contains("no error");
	}

	public String getLastErrorString()
	{
		synchronized (mLock)
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
	}

	public void printLastError()
	{
		synchronized (mLock)
		{
			ASDKLibrary.asdkPrintLastError();
		}
	}

	public boolean isDebugPrintout()
	{
		return mDebugPrintout;
	}

	public void setDebugPrintout(boolean pDebugPrintout)
	{
		mDebugPrintout = pDebugPrintout;
	}

	/**
	 * Checks vector dimensions and throw an exception if the length is incorrect.
	 * 
	 * @param pVector
	 *          vector to check for correct length (Java double array)
	 * @param pExpectedVectorLength
	 *          expected correct length
	 */
	private void checkVectorDimensions(	Pointer<Double> pVector,
																			int pExpectedVectorLength)
	{
		if (pVector.getValidElements() != pExpectedVectorLength)
		{
			String lExceptionMessage = String.format(	"Provided vector has wrong length %d should be %d",
																								pVector.getValidElements(),
																								pExpectedVectorLength);
			throw new AlpaoException(lExceptionMessage);
		}
	}

}
