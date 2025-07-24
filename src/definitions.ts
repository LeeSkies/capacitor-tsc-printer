export interface PrinterPlugin {
  /**
   * Print a PDF file from a file path
   * @param options - Print options including IP address, port, and file URI
   * @returns Promise that resolves when printing is complete
   */
  print(options: PrintOptions): Promise<void>;

  /**
   * Print a PDF from base64 string with basic positioning
   * @param options - Print options including base64 PDF data and positioning
   * @returns Promise that resolves when printing is complete
   */
  printPdf(options: PrintPdfOptions): Promise<void>;

  /**
   * Print a PDF from base64 string with full setup configuration
   * @param options - Print options including base64 PDF data, positioning, and printer setup
   * @returns Promise that resolves when printing is complete
   */
  printPdfWithSetup(options: PrintPdfWithSetupOptions): Promise<void>;
}

export interface PrintOptions {
  /** IP address of the TSC printer */
  IPAddress: string;
  /** Port number for printer connection */
  port: number;
  /** File URI/path to the PDF file */
  uri: string;
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

export interface PrintPdfWithSetupOptions {
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
  /** Label width in dots */
  width: number;
  /** Label height in dots */
  height: number;
  /** DPI (dots per inch) for printing quality */
  dpi: number;
}