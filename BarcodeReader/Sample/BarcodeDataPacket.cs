#nullable enable

using System;
using System.IO;
using System.Net;
using System.Text;
using UnityEngine;

namespace BarcodeReader
{
    public struct BarcodeDataPacket
    {
        public long UnixTime { get; set; }
        public Vector2[] CornerPoints { get; set; }
        public string RawValue { get; set; }

        public static bool TryReadFromBinaryReader(BinaryReader dataInputStream, out BarcodeDataPacket packet)
        {
            var readPacket = ReadFromBinaryReader(dataInputStream);
            packet = readPacket ?? new();

            return readPacket != null;
        }

        public static BarcodeDataPacket? ReadFromBinaryReader(BinaryReader dataInputStream)
        {
            var rawValueSize = IPAddress.NetworkToHostOrder(dataInputStream.ReadInt32());
            if (rawValueSize <= 0)
            {
                return null;
            }

            var unixTime = IPAddress.NetworkToHostOrder(dataInputStream.ReadInt64());

            var cornerPoints = new Vector2[4];
            cornerPoints[0] = ReadVector2(dataInputStream);
            cornerPoints[1] = ReadVector2(dataInputStream);
            cornerPoints[2] = ReadVector2(dataInputStream);
            cornerPoints[3] = ReadVector2(dataInputStream);

            byte[] rawValueBytes = new byte[rawValueSize];
            int bytesRead = 0;
            while (bytesRead < rawValueSize)
            {
                int bytesToRead = Mathf.Min(rawValueSize - bytesRead, 1024);
                int result = dataInputStream.BaseStream.Read(rawValueBytes, bytesRead, bytesToRead);
                if (result == -1)
                {
                    return null;
                }
                bytesRead += result;
            }

            string rawValue = Encoding.UTF8.GetString(rawValueBytes);
            return new BarcodeDataPacket()
            {
                UnixTime = unixTime, 
                CornerPoints = cornerPoints, 
                RawValue = rawValue
            };        
        }

        public static Vector2 ReadVector2(BinaryReader reader)
        {
            float x = ReadBigEndianFloat(reader);
            float y = ReadBigEndianFloat(reader);
            return new Vector2(x, y);
        }

        private static float ReadBigEndianFloat(BinaryReader reader)
        {
            var floatBytes = reader.ReadBytes(4);
            Array.Reverse(floatBytes);
            return BitConverter.ToSingle(floatBytes, 0);            
        }
    }
}