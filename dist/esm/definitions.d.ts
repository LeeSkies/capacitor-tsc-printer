export interface PrinterPlugin {
    /**
     * Print a PDF from base64 string via network connection
     * @param options - Print options including base64 PDF data and positioning
     * @returns Promise that resolves when printing is complete
     */
    printPDFByNetwork(options: PrintPdfOptions): Promise<void>;
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
