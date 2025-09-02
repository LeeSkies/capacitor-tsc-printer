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
     * @param options - Optional discovery options (all parameters have defaults)
     * @returns Promise that resolves with list of discovered printers
     */
    discoverNetworkPrinters(options?: DiscoveryOptions): Promise<DiscoveryResult>;
    /**
     * Configure static IP address for a network printer (auto-detects subnet mask and gateway)
     * @param options - Static IP configuration options
     * @returns Promise that resolves with the configured static IP details
     */
    configureStaticIP(options: StaticIPOptions): Promise<StaticIPResult>;
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
export interface BaseDiscoveryOptions {
    /** Timeout in milliseconds for printer discovery (default: 5000ms) */
    timeoutMs?: number;
}
export interface FirstFoundDiscoveryOptions extends BaseDiscoveryOptions {
    /** Return only the first printer found for faster discovery */
    returnFirst: true;
}
export interface TargetMacDiscoveryOptions extends BaseDiscoveryOptions {
    /** Return immediately when a printer with this MAC address is found */
    targetMacAddress: string;
}
export declare type DiscoveryOptions = BaseDiscoveryOptions | FirstFoundDiscoveryOptions | TargetMacDiscoveryOptions;
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
export interface StaticIPOptions {
    /** Current IP address of the printer to configure */
    printerIP: string;
    /** Port number for printer connection (default: 9100) */
    port?: number;
    /** New static IP address to assign to the printer */
    staticIP: string;
}
export interface StaticIPResult {
    /** Whether the configuration was sent successfully */
    success: boolean;
    /** The static IP address that was configured */
    staticIP: string;
    /** Auto-detected gateway that was used */
    gateway: string;
    /** Auto-detected subnet mask that was used */
    subnetMask: string;
    /** Status message about the configuration */
    message: string;
}
