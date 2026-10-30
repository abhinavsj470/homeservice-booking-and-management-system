using System;
using System.IO;
using System.IO.Compression;
using System.Diagnostics;
using System.Windows.Forms;
using System.Drawing;
using System.Reflection;
using System.Threading;

public class SetupForm : Form {
    private ProgressBar pBar;
    private Label lblStatus;

    public SetupForm() {
        this.Text = "Home Service System - Installer";
        this.Size = new Size(460, 200);
        this.FormBorderStyle = FormBorderStyle.FixedDialog;
        this.StartPosition = FormStartPosition.CenterScreen;
        this.MaximizeBox = false;
        this.BackColor = Color.White;

        Label lblHeader = new Label() {
            Text = "Home Service Booking & Management System",
            Font = new Font("Segoe UI", 12, FontStyle.Bold),
            ForeColor = Color.FromArgb(15, 23, 42),
            Location = new Point(24, 20),
            AutoSize = true
        };
        this.Controls.Add(lblHeader);

        lblStatus = new Label() {
            Text = "Preparing installation...",
            Font = new Font("Segoe UI", 9, FontStyle.Regular),
            ForeColor = Color.FromArgb(100, 116, 139),
            Location = new Point(24, 52),
            Size = new Size(400, 20)
        };
        this.Controls.Add(lblStatus);

        pBar = new ProgressBar() {
            Location = new Point(24, 80),
            Size = new Size(396, 22),
            Style = ProgressBarStyle.Marquee,
            MarqueeAnimationSpeed = 30
        };
        this.Controls.Add(pBar);

        Label lblFooter = new Label() {
            Text = "Standalone package • No Java installation required",
            Font = new Font("Segoe UI", 8, FontStyle.Italic),
            ForeColor = Color.FromArgb(148, 163, 184),
            Location = new Point(24, 118),
            AutoSize = true
        };
        this.Controls.Add(lblFooter);

        this.Shown += (s, e) => {
            Thread t = new Thread(DoInstall);
            t.IsBackground = true;
            t.Start();
        };
    }

    private void DoInstall() {
        try {
            UpdateStatus("Extracting application package to your computer...");
            string localAppData = Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData);
            string installDir = Path.Combine(localAppData, "HomeService");

            if (!Directory.Exists(installDir)) {
                Directory.CreateDirectory(installDir);
            }

            // Extract embedded ZIP payload
            Assembly asm = Assembly.GetExecutingAssembly();
            using (Stream s = asm.GetManifestResourceStream("payload.zip")) {
                if (s == null) {
                    throw new Exception("Installation payload not found inside installer.");
                }

                string tempZip = Path.Combine(Path.GetTempPath(), "HomeService_temp.zip");
                using (FileStream fs = new FileStream(tempZip, FileMode.Create, FileAccess.Write)) {
                    s.CopyTo(fs);
                }

                UpdateStatus("Installing files and embedded runtime...");
                ZipFile.ExtractToDirectory(tempZip, installDir);
                try { File.Delete(tempZip); } catch {}
            }

            // Check executable path (it might be in installDir or installDir\HomeService)
            string exePath = Path.Combine(installDir, "HomeService.exe");
            if (!File.Exists(exePath)) {
                string nested = Path.Combine(installDir, "HomeService", "HomeService.exe");
                if (File.Exists(nested)) {
                    exePath = nested;
                    installDir = Path.Combine(installDir, "HomeService");
                }
            }

            UpdateStatus("Creating Desktop and Start Menu shortcuts...");
            CreateShortcut("Home Service System.lnk", exePath, installDir);

            UpdateStatus("Launching Home Service System...");
            Thread.Sleep(800);

            Process.Start(new ProcessStartInfo() {
                FileName = exePath,
                WorkingDirectory = installDir
            });

            this.Invoke((MethodInvoker)delegate {
                this.Close();
            });
        } catch (Exception ex) {
            MessageBox.Show("Installation failed:\n" + ex.Message, "Installer Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            this.Invoke((MethodInvoker)delegate {
                this.Close();
            });
        }
    }

    private void CreateShortcut(string shortcutName, string targetPath, string workingDir) {
        try {
            Type shellType = Type.GetTypeFromProgID("WScript.Shell");
            if (shellType != null) {
                dynamic shell = Activator.CreateInstance(shellType);
                string desktop = Environment.GetFolderPath(Environment.SpecialFolder.DesktopDirectory);
                var sc = shell.CreateShortcut(Path.Combine(desktop, shortcutName));
                sc.TargetPath = targetPath;
                sc.WorkingDirectory = workingDir;
                sc.Description = "Home Service Booking and Management System";
                sc.Save();
            }
        } catch {}
    }

    private void UpdateStatus(string text) {
        if (this.IsHandleCreated) {
            this.Invoke((MethodInvoker)delegate {
                lblStatus.Text = text;
            });
        }
    }

    [STAThread]
    public static void Main() {
        Application.EnableVisualStyles();
        Application.SetCompatibleTextRenderingDefault(false);
        Application.Run(new SetupForm());
    }
}
