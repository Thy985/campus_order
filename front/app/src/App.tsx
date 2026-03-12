import { RouterProvider } from 'react-router-dom';
import { Toaster } from '@/components/ui/sonner';
import { router } from '@/router';

function App() {
  return (
    <>
      <RouterProvider router={router} />
      <Toaster 
        position="top-center"
        toastOptions={{
          duration: 3000,
          style: {
            background: '#363636',
            color: '#fff',
          },
        }}
      />
    </>
  );
}

export default App;
