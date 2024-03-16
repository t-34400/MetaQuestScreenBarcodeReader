#nullable enable

using UnityEngine;
using TMPro;

namespace BarcodeReader
{
    class BarcodePacketLogger : MonoBehaviour
    {
        private const string FORMAT = "Delay(ms): {0}\nRaw Value: {1}\nCorner: {2}";
        [SerializeField] private TMP_Text text = default!;

        public void LogNewPacket(long unixTime, BarcodeDataPacket packet)
        {
            var cornerPoints = packet.CornerPoints;
            text.text = string.Format(FORMAT, unixTime - packet.UnixTime, packet.RawValue, string.Join(',', cornerPoints));
        }
    }
}