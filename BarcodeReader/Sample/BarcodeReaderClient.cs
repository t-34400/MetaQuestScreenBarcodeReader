#nullable enable

using System;
using System.Collections;
using System.IO;
using System.Net;
using System.Net.Sockets;
using UnityEngine;
using UnityEngine.Events;

namespace BarcodeReader
{
    public class BarcodeReaderClient : MonoBehaviour
    {
        private const int START_MESSAGE = 0;
        private const int STOP_MESSAGE = 1;

        [SerializeField] private string remoteAddressStr = "127.0.0.1";
        [SerializeField] private int remotePort = 3250;
        [SerializeField] private float tryConnectInterval = 5f;
        [SerializeField] private bool startOnConnected = true;
        [SerializeField] private UnityEvent<long, BarcodeDataPacket> packetReceived = default!;

        private IEnumerator? currentConnectCoroutine;
        private bool isConnected = false;

        private TcpClient? client;
        private NetworkStream? stream;
        private BinaryReader? reader;

        public bool StartScanning() => SendMessage(START_MESSAGE);
        public void StopScanning() => SendMessage(STOP_MESSAGE);

        private bool SendMessage(int message)
        {
            if (stream == null)
            {
                return false;
            }
            try
            {
                byte[] zero = BitConverter.GetBytes(message);
                stream.Write(zero, 0, zero.Length);
                return true;
            }
            catch (Exception e)
            {
                Debug.Log($"Failed to send a message: {message}. " + e.Message, this);
                CloseSocket();
                return false;
            }
        }

        private void Update()
        {
            if (!isConnected)
            {
                if (currentConnectCoroutine == null)
                {
                    currentConnectCoroutine = TryConnectCoroutine();
                    StartCoroutine(currentConnectCoroutine);
                }
                return;
            }

            if (reader == null || stream == null)
            {
                CloseSocket();
                return;
            }

            try
            {
                if (!stream.DataAvailable)
                {
                    return;
                }

                if (BarcodeDataPacket.TryReadFromBinaryReader(reader, out var packet))
                {
                    var unixTime = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
                    packetReceived.Invoke(unixTime, packet);
                }
            }
            catch (Exception e)
            {
                Debug.Log("Failed to read a barcode data packet." + e.Message, this);
                CloseSocket();
            }
        }

        private void CloseSocket()
        {
            isConnected = false;

            reader?.Close();
            reader?.Dispose();
            reader = null;

            stream?.Close();
            stream?.Dispose();
            stream = null;

            client?.Close();
            client?.Dispose();
            client = null;
        }

        private IEnumerator TryConnectCoroutine()
        {
            var remoteAddress = IPAddress.Parse(remoteAddressStr);
            while (!isConnected)
            {
                try
                {
                    client = new TcpClient();
                    client.Connect(remoteAddress, remotePort);
                    stream = client.GetStream();
                    reader = new BinaryReader(client.GetStream());
                    isConnected = true;
                    if (startOnConnected)
                    {
                        StartScanning();
                    }
                    break;
                }
                catch (Exception e)
                {
                    Debug.Log("Failed to connect to the server." + e.Message, this);
                }
                yield return new WaitForSeconds(tryConnectInterval);
            }
            currentConnectCoroutine = null;
        }
    }
}