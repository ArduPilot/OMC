using System.Windows;
using System.Windows.Media.Imaging;

namespace LizardTech.SampleWpfViewer
{
   public partial class MainWindow : Window
   {
      private Interop m_interop;

      public MainWindow()
      {
         InitializeComponent();

         this.DataContext = this;

         m_interop = new Interop();
      }

      private void OpenFileClick(object sender, RoutedEventArgs e)
      {
         Microsoft.Win32.OpenFileDialog dlg = new Microsoft.Win32.OpenFileDialog();
         dlg.Multiselect = false;
         dlg.DefaultExt = ".sid";
         dlg.Filter = "MrSID Image (.sid)|*.sid";
         if (dlg.ShowDialog() == false)
         {
            // user didn't select a file
            return;
         }
         string fileName = dlg.FileName;

         m_interop.Close();

         m_interop.Open(fileName);

         UpdateBitmap();

         return;
      }

      private void UpdateBitmap()
      {
         WriteableBitmap bitmap = m_interop.CreateBitmap((int)RenderingCanvas.ActualWidth, (int)RenderingCanvas.ActualHeight);
         if (bitmap != null)
         {
            imageControl.Source = bitmap;
         }

         return;
      }
   }
}
