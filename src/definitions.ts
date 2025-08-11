export interface PrinterPlugin {
  /**
   * Print a PDF from base64 string via network connection
   * @param options - Print options including base64 PDF data and positioning
   * @returns Promise that resolves when printing is complete
   */
  printPDFByNetwork(options: PrintPdfOptions): Promise<void>;

  /**
   * Print a PDF from base64 string via USB connection
   * @param options - Print options including base64 PDF data and positioning (no IP/port needed)
   * @returns Promise that resolves when printing is complete
   */
  printPDFByUSB(options: PrintPdfUSBOptions): Promise<void>;

  /**
   * Discover available network printers using UDP broadcast
   * @param options - Discovery options including timeout
   * @returns Promise that resolves with list of discovered printers
   */
  discoverNetworkPrinters(options: DiscoveryOptions): Promise<DiscoveryResult>;
}

export interface PrintPdfOptions {
  /** IP address of the TSC printer */
  IPAddress: string;
  /** Port number for printer connection */
  port: number;
  /** Base64 encoded PDF data */
  base64String: string;
  /** X offset position for printing */
  offsetX: number;
  /** Y offset position for printing */
  offsetY: number;
  /** DPI (dots per inch) for printing quality */
  dpi: number;
}

export interface PrintPdfUSBOptions {
  /** Base64 encoded PDF data */
  base64String: string;
  /** X offset position for printing */
  offsetX: number;
  /** Y offset position for printing */
  offsetY: number;
  /** DPI (dots per inch) for printing quality */
  dpi: number;
}

export interface DiscoveryOptions {
  /** Timeout in milliseconds for printer discovery (default: 5000ms) */
  timeoutMs?: number;
}

export interface PrinterInfo {
  /** IP address of the discovered printer */
  ipAddress: string;
  /** Printer model name or identifier */
  name: string;
  /** MAC address of the printer */
  macAddress: string;
  /** Current status of the printer */
  status: string;
}

export interface DiscoveryResult {
  /** Array of discovered printers */
  printers: PrinterInfo[];
  /** Total number of printers found */
  count: number;
}